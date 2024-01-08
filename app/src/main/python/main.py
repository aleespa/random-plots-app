import random
import importlib
import json
import os

script_dir = os.path.dirname(os.path.realpath(__file__))

json_file_light = os.path.join(script_dir,'scripts_light.json')
json_file_dark = os.path.join(script_dir,'scripts_dark.json')
with open(json_file_light, 'r') as file:
    light_mode_scripts = json.load(file)
with open(json_file_dark, 'r') as file:
    dark_mode_scripts = json.load(file)
def generate(dark_mode):
    if dark_mode:
        scripts = dark_mode_scripts
    else:
        scripts = light_mode_scripts
    selected_script_name = random.choice(list(scripts.keys()))

    selected_script = importlib.import_module(selected_script_name)

    image = selected_script.create_image()
    with open(os.path.join(script_dir,scripts.get(selected_script_name)), 'r') as file:
        tex_content = file.read()

    return image, tex_content

