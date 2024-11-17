import os
import importlib
script_dir = os.path.dirname(os.path.realpath(__file__))

def generate(dark_mode, script):
    selected_script = importlib.import_module(f"figures.{script}")
    image = selected_script.create_image(dark_mode)
    return image

