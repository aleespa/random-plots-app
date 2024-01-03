import random
import importlib

def generate(dark_mode):
    if not dark_mode:
        scripts = ["script_1"]
    else:
        scripts = ["script_2"]
    selected_script_name = random.choice(scripts)

    selected_script = importlib.import_module(selected_script_name)

    image = selected_script.create_image()

    return image, dark_mode

