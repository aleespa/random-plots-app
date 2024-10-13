import os
import spirograph
script_dir = os.path.dirname(os.path.realpath(__file__))

def generate(dark_mode):
    image = spirograph.create_image(dark_mode)
    with open(os.path.join(script_dir,'script_1.tex'), 'r') as file:
        tex_content = file.read()

    return image, tex_content

