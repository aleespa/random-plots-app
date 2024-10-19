import os
import importlib
script_dir = os.path.dirname(os.path.realpath(__file__))

def generate(dark_mode, script):
    selected_script = importlib.import_module(script)
    image = selected_script.create_image(dark_mode)
    with open(os.path.join(script_dir,'script_1.tex'), 'r') as file:
        tex_content = file.read()

    return image, tex_content

