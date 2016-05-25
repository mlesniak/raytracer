[![Build Status](https://travis-ci.org/mlesniak/raytracer.svg?branch=master)](https://travis-ci.org/mlesniak/raytracer)

# Introduction

A simple raytracer. All algorithms, ideas and designs are motivated by the book

> An Introduction to Ray Tracing
> Andrew S. Glassner et al.
> 1989, The Morgan Kaufmann Series in Computer Graphics

Note that the current state of this project is :boom: hacky :boom:, i.e. the code is not yet refactored, structured or 
documented.

# Gallery

These images show progress and bugs while developing.

![Commit 1fd3495](gallery/image-1fd3495.png?raw=true)
![Commit 460f043](gallery/image-460f043.png?raw=true)
![Commit bc76514](gallery/image-bc76514.png?raw=true)
![Commit 70c56f1](gallery/image-70c56f1.png?raw=true)
![Commit 3b7f1a3](gallery/image-3b7f1a3.png?raw=true)
![Commit b08068b](gallery/image-b08068b.png?raw=true)

# Quality

As part of the build process we check the code quality using

- Checkstyle (in particular for source code formatting)
- FindBugs

The corresponding configuration files are stored in ```src/main/resrouces/codestyle```. If any of these tools emit a 
warning, the build fails.

# License

Copyright (c) 2016 Michael Lesniak, licensed under the Apache License.

# Todo and planned features

- ~~Implement standard FoV / Camera pattern~~
- Gouraud Shading
- Phong Shading
- Shadows / Lightning
- Reflections
- Materials such as glass
- Plane as geometric object
- Texture mapping
- Antialiasing
- Procedural generation
- Animation support
- Support for external file formats (SketchUp? Blender?)
- ~~negative z-axis goes into the scene~~
- ~~Parallelization~~
