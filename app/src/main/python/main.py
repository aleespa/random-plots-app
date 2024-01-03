import random
import importlib

def generate(dark_mode):
    if dark_mode:
        scripts = ["script_2"]
    else:
        scripts = ["script_1"]
    selected_script_name = random.choice(scripts)

    selected_script = importlib.import_module(selected_script_name)

    image = selected_script.create_image()

    return image, dark_mode

