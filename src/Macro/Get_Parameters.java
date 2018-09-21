macro "Get_Parameters"{

    //Chose analysis type

    //Path of the file of the preset Analysis modes
    PathSettings = getDirectory("macros");
    PathSettings += "Droplets"+File.separator;
    PathSettings += "settings.csv";

    //Retrieve content of settings.csv
    myText = File.openAsString(PathSettings);
    //separate lines
    mylines = split(myText, "\n");

    //List of all registered analysis mode
    AnalyisType = newArray();

    //Retrieve settings names
    //first column of each line (header excepted)
    for(line = 1; line <mylines.length; line++){

        //Get the name of the analysis mode
         mycolumns = split(mylines[line], ",");

         //Feed the listing
         AnalyisType = Array.concat(AnalyisType,mycolumns[0]);
    }

    /*
    Parameters structure:
    ====================
    0   Analysis type
    1   Extension
    2   Reference resoltion (micron/pixel) in X
    3   Reference resoltion (micron/pixel) in Y
    4   Distance xy in pixels between 2 particles
    5   Distance in z between 2 particles
    6   Minimum size in pixel
    7   Maximum size in pixel
    8   Maximum size (to exclude big fat bodies)
    9   Minimum circularity
    10   Maximum circularity
    11  Number of Iterations
    12  Zone for enlargement (in pixel) and erasing
    13  Number of bins for distributions
    14  Type of the analysis Zone
    15  Minimal number of particules to continue iterations
    16  Enhance signal
    */

    //Select analysis type window
    Dialog.create("ANALYSIS:");
    Dialog.addMessage("Specify what kind of analysis you are performing:");
    Dialog.addMessage("");
    Dialog.addChoice("ANALYSIS: ", AnalyisType, "Lipid Droplets");
    Dialog.show();

    //Chosen Analysis mode
    myAnalysistype = Dialog.getChoice();

    //Retrieve Parameters
    param = ""

    //Identify the line corresponding to the chosen analysis mode
    for(line = 1; line <mylines.length; line++){
         mycolumns = split(mylines[line], ",");

         if (mycolumns[0] == myAnalysistype){
             param = mylines[line];
         }
    }

    //Return the parameters for the main GUI
    return param;
}
