#!/usr/bin/env python3
# coding: utf8

import argparse
import os
import sys
from copy import deepcopy

import yaml


USERNAME = os.getenv('USER')
HOME = os.path.expanduser(f'~{USERNAME}')

SC_DIR = os.path.join(HOME, '.shortcut')
INSTANCE_DIR = os.path.join(SC_DIR, 'instances')
TEMPLATE_FILE = os.path.join(SC_DIR, 'template.yaml')

__VERSION__ = 'v0.1.0'


class ArgumentError(Exception):
    pass


class Shortcut:
    """
    Class defining
    """
    def __init__(self, shortcut, yaml_config):
        self.name = shortcut
        self.description = yaml_config['description']
        self.instances = yaml_config['instances']
        self.command = yaml_config['command']

    def print_instances(self):
        for instance, args in self.instances.items():
            print("  {:<20} {:<15} ".format(instance, args['description']))
            # print(instance, args['description'])

    def print_help(self):
        print("Shortcut: sc f{self.name}")
        print(self.description, end='\n\n')
        print("command to be run: ")
        print(self.command, end='\n\n')
        print(f"{self.name} instances:")
        self.print_instances()

    def format_command(self, instance, args):
        """
        Format command to include all additional parameters passed in and
        instance arguments, if the
        """
        if instance not in self.instances:
            raise ValueError("Error: such instance: {instance}")
        instance = deepcopy(self.instances[instance])
        if "REST" in instance['args']:
            instance['args']["REST"].extend(args)
        else:
            instance['args']["REST"] = args

        # Reformat "REST" to be string split by spaces rather than a list
        # since we want a cli command with 'a b c' not ['a', 'b', ... ]
        instance['args']["REST"] = ' '.join(instance['args']["REST"])
        return self.command.format(**instance['args'])


def make_directory(file_path: str):
    """
    Creates the directory at the path:
    :param file_path: the path of the directory that you want to create
    """
    try:
        os.makedirs(file_path, exist_ok=True)
    except OSError as exc:
        if exc.errno == errno.EEXIST and os.path.isdir(file_path):
            pass
        else:
            print("Error while attempting to create directory: {file_path}")
            exit(3)


def initialize():
    """ Method for creating initiale file structure for dotfiles """
    make_directory(SC_DIR)
    make_directory(INSTANCE_DIR)
    if os.path.exists(TEMPLATE_FILE):
        return
    with open(TEMPLATE_FILE, 'w+') as file:
        file.write("""# Define the name of your shortcut here
name: template

# Brief description of what the command does
description: |-
  This command runs command_name

  sc template [ARG1] [REST]...

  more information ...

# Multiline String containing
command: |-
  command_name {arg1} other_command {arg2} {REST}

# Define the instances of this short cut
instances:

  # First instance (called by sc template first_name)
  first_name:
    # Short description of the first instance
    description: |-
      this is the first instance

    # mapping of arguments to values,
    args:
      arg1: '--arg1 additional_stuff'
      arg2: 'arg2'
      arg3: '' # Can have a useless argument

      # REST is a keyword for where you want any additional arguments
      # to be passed to (as a list)
      REST:
        - 'rest1'
        - 'rest2'

  # repeat with additional instances
  second:
    description: |-
      this is the second instance
    args:
      arg1: arg1
      arg2: arg2
      arg3: arg3 """)


def list_shortcuts():
    """ List all currently defined shortcuts """

    def get_file_name(full_path):
        """ Get the name of the file without its extension or path """
        basename = os.path.basename(full_path)
        return os.path.splitext(basename)[0]

    for file in os.listdir(INSTANCE_DIR):
        if not file.endswith('.yaml'):
            continue
        yield get_file_name(file)


def process_base_args(args):
    """ Process all flags for just sc itself """
    if args.version:
        print(f'sc {__VERSION__}')
    elif args.list:
        for shortcuts in list_shortcuts():
            print(shortcuts)
    # Open the file up with a text editor if it exists
    elif args.edit:
        if args.edit in list_shortcuts():
            yaml_file = os.path.join(INSTANCE_DIR, f'{args.edit}.yaml')
            os.system(f'$EDITOR {yaml_file}')
        else:
            print(f'Error: "{args.edit}" is not a valid shortcut',
                  file=sys.stderr)
    # Delete a yaml file if it exists
    elif args.delete:
        if args.delete in list_shortcuts():
            yaml_file = os.path.join(INSTANCE_DIR, f'{args.delete}.yaml')
            os.remove(yaml_file)
        else:
            print(f'Error: "{args.delete}" is not a valid shortcut',
                  file=sys.stderr)
    # Create a new shortcut file if it exists
    elif args.add:
        if args.add in list_shortcuts():
            print(f'Error: "{args.add}" already exists',
                  file=sys.stderr)
        else:
            yaml_file = os.path.join(INSTANCE_DIR, f'{args.add}.yaml')
            os.system(f'cp {TEMPLATE_FILE} {yaml_file}')


def load_shortcut(shortcut):
    """ Parses config file of a shortcut """
    yaml_file = os.path.join(INSTANCE_DIR, f'{shortcut}.yaml')
    with open(yaml_file, 'r') as file:
        config = yaml.safe_load(file.read())
    return Shortcut(shortcut, config)


def main():
    """ Main function for shortcut """
    initialize()

    desc = f'shortcut [sc] {__VERSION__}'
    usage_msg = """sc [shortcut] [options] [instance] [options]
Allows you to define shortcuts for various commands using yaml files"""

    subcommand_help = """
List of subcommands: add, delete, or a directory entry to cd to"""
    parser = argparse.ArgumentParser(description=desc, usage=usage_msg)

    parser.add_argument('--version', action='version', default=False,
                        help='Displays the current version of sc',
                        version=__VERSION__)
    parser.add_argument("-l", "--list", action="store_true", default=False,
                        help="Displays all current shortcuts",
                        dest="list")
    parser.add_argument('-a', "--add", action="store", default=None,
                        dest='add', help='Creates a new shortcut')
    parser.add_argument('-e', "--edit", action="store", default=None,
                        dest='edit', help='Edits the yml config of a shortcut')
    parser.add_argument('-d', "--delete", action="store", default=None,
                        dest='delete', help='Deletes a shortcut')

    def override_error_function(error):
        """ Function to replace the default parser error function """
        raise ArgumentError(error)
    parser.error = override_error_function

    if len(sys.argv) == 1:
        parser.print_help()
        sys.exit(0)

    try:
        args = parser.parse_args(sys.argv[1:])
    except ArgumentError as e:
        # Check to see if it is a flag, if it is, then its not an instance,
        # and is therefor an invalid argument
        if sys.argv[1].startswith('-'):
            print(e, file=sys.stderr)
            sys.exit(1)
        elif len(sys.argv) > 2 and sys.argv[2].startswith('-'):
            print(e, file=sys.stderr)
            sys.exit(1)
    else:
        process_base_args(args)
        sys.exit(0)

    shortcut = sys.argv[1]
    if shortcut not in list_shortcuts():
        print("Invalid shortcut", file=sys.stderr)

    shortcut = load_shortcut(shortcut)

    # Handle case of 'sc <shortcut>' by just printing the help
    if len(sys.argv) == 2:
        shortcut.print_help()
        sys.exit(0)

    instance = sys.argv[2]
    try:
        command = shortcut.format_command(instance, sys.argv[3:])
    except ValueError as e:
        print(e, file=sys.stderr)
        sys.exit(1)
    os.system(command)


if __name__ == "__main__":
    main()
