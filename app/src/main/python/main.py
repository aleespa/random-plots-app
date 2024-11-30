import os
import importlib
script_dir = os.path.dirname(os.path.realpath(__file__))

def generate(seed, dark_mode, color, script):
    selected_script = importlib.import_module(f"figures.{script}")
    image = selected_script.create_image(seed, dark_mode, color)
    return image
