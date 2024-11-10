![Build Status](https://github.com/joebalanoff/UMLVisualizer/actions/workflows/maven-publish.yml/badge.svg)

# UMLVisualizer
UMLVisualizer is a Java-based tool that dynamically generates UML diagrams from Java code. It parses Java files, extracts key information, and displays it in an intuitive, navigable canvas. Designed to streamline code analysis and improve documentation, UMLVisualizer adapts to various project structures with minimal configuration.

## Key Features
Automatic Parsing: UMLVisualizer scans Java files in the specified directory, extracting class hierarchies, methods, and fields with ease.
Interactive Visualization: Displays UML diagrams with panning, zooming, and smooth movement for enhanced navigation.
Customizable Display: Classes are color-coded based on hierarchy, with abstract classes and subclasses visually connected.

## Installation & Usage
Import the UMLVisualizer package into your project.
Initialize the tool with:
```java
UMLVisualizer.init();
``` 
UMLVisualizer will automatically parse Java files in the directory and onward, rendering a UML diagram on an interactive canvas.

## Benefits
UMLVisualizer offers developers and technical leads an accessible way to visualize class structures and relationships, facilitating code reviews and design discussions. By providing a clear visual representation, it supports faster onboarding, code comprehension, and project documentation.
