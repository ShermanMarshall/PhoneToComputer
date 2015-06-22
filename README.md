# PhoneToComputer
Android app for sending data between device and another computer

The supplied Java class "PhoneConnection" creates a ServerSocket on the host computer, enabling it to communicate with the
Android device. PhoneConnection is meant to be run from the command-line, and minimally requires supply of an appropriate 
IP address per the machine's configured network interfaces--additional arguments are interpreted as an attempt to send 
data to the Android device, rather than receiving it.
