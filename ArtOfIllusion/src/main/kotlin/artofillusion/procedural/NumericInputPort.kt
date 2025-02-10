package artofillusion.procedural

class NumericInputPort(location: Int, vararg description: String) :
    IOPort(NUMBER, INPUT, location, *description)
