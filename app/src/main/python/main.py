import random
import importlib

def generate(dark_mode):
    if dark_mode:
        scripts = {"script_2":"Eigenvalues of a random matrix. The random values are sampled from"
                   "the unit circle"}
    else:
        scripts = {"script_1":"Eigenvalues of a random matrix. The random values are sampled from"
                              "the unit circle"}
    selected_script_name = random.choice(list(scripts.keys()))

    selected_script = importlib.import_module(selected_script_name)

    image = selected_script.create_image()

    return image, scripts.get(selected_script_name)

