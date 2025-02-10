package artofillusion.procedural

class ColorInputPort(location: Int, vararg description: String) :
    IOPort(COLOR, INPUT, location, *description)
