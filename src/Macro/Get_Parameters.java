macro "Get_Parameters"{

    //Chose analysis type

    PathSettings = getDirectory("macros");
    PathSettings += "Droplets"+File.separator;
    PathSettings += "settings.csv";

    //Retrieve content of settings.csv
    myText = File.openAsString(PathSettings);
    //separate lines
    mylines = split(myText, "\n");

    AnalyisType = newArray();

    //Retrieve settings names
    for(line = 1; line <mylines.length; line++){
         mycolumns = split(mylines[line], ",");
         AnalyisType = Array.concat(AnalyisType,mycolumns[0]);
    }

    /*
    Parameters structure:
    ====================
    0   Extension
    1   Reference resoltion (micron/pixel) in X
    2   Reference resoltion (micron/pixel) in Y
    3   Distance xy in pixels between 2 particles
    4   Distance in z between 2 particles
    5   Minimum size in pixel
    6   Maximum size in pixel
    7   Maximum size (to exclude big fat bodies)
    8   Minimum circularity
    9   Maximum circularity
    10  Number of Iterations
    11  Zone for enlargement (in pixel) and erasing
    12  Number of bins for distributions
    13  Type of the analysis Zone
    14  Minimal number of particules to continue iterations
    */


    Dialog.create("ANALYSIS:");
    Dialog.addMessage("Specify what kind of analysis you are performing:");
    Dialog.addMessage("");
    Dialog.addChoice("ANALYSIS: ", AnalyisType, "Lipid Droplets");
    Dialog.show();

    myAnalysistype = Dialog.getChoice();

    //Retrieve Parameters
    param = ""

    for(line = 1; line <mylines.length; line++){
         mycolumns = split(mylines[line], ",");

         if (mycolumns[0] == myAnalysistype){
             param = mylines[line];
         }
    }

    return param;
}
