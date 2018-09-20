macro "Main"{
    //INFOS
    tag = "v5.0.0"
    lastStableCommit = "345f8c88"
    gitlaburl = "http://gitlab.biologie.ens-lyon.fr/dcluet/Lipid_Droplets"

    /*
    ============================================================================
                        QC TESTS
    ============================================================================

    Linux 16.04 LTS
    ~~~~~~~~~~~~~~~
    Lipid Droplets Brain (Whole Tissue with Sub-selection): 2018/09/19
    Lipid Droplets Brain (Whole Tissue): 2018/09/19
    Lipid Droplets Brain (Manual ROI): 2018/09/19
    Lipid Droplets Retina (Manual ROI): 2018/09/19
    REPO (Whole Tissue): 2018/09/19

    Windows 10
    ~~~~~~~~~~
    Lipid Droplets Brain (Whole Tissue with Sub-selection):
    Lipid Droplets Brain (Whole Tissue):
    Lipid Droplets Brain (Manual ROI):
    Lipid Droplets Retina (Manual ROI):
    REPO (Whole Tissue):

    */

    qcl = "16.04 LTS On Progress";
    qcp = "WINDOWS 10 On Progress";
    qcm = "On progress";

    /*
    ============================================================================
                        PATHS OF ACCESSORY MACROS
    ============================================================================
    */

    //Folder of the macro
    PM = getDirectory("macros");
    PM += "Droplets"+File.separator;

    //Clear all Images
    PathM3 = PM + "Close_Images.java";

    //Launch Choice of Analysis
    PathGetParam = PM + "Get_Parameters.java";

    //Launch GUI
    PathGUI = PM + "Main_GUI.java";

    //Launch Files identification
    PathIdent = PM + "Identify_Files.java";

    //Select Channel
    PathSelChannel = PM + "Select_Channel.java";

    //Launch Manual Taylor
    PathTaylor = PM + "Taylor.java";

    //Run Lipid_Droplets
    PathLD = PM + "Lipid_Droplets.java";

    //Run stats analysis
    PathMS = PM + "Stats.java";

    /*
    ============================================================================
                                CLEAN IMAGEJ
    ============================================================================
    */

    //Close all non required images.
    runMacro(PathM3);

    //Clean roiManager
    roiManager("reset");

    /*
    ============================================================================
                                WELCOME WINDOW
    ============================================================================
    */

    Welcome(tag, lastStableCommit, qcl, qcp, qcm, gitlaburl);

    /*
    ============================================================================
                        SELECT ANALYSIS TYPE AND GET SETTINGS
    ============================================================================
    */

    param = runMacro(PathGetParam);

    /*
    ============================================================================
                        MAIN GUI AND FOLDER IDENTIFICATION
    ============================================================================
    */

    //Display GUI and retrieve parameters as a single string
    ResultGUI = runMacro(PathGUI, param);

    //Extract key parameters and command line
    myParameters = split(ResultGUI, "\n");

    //Reuse old manual selection?
    myReuse = myParameters[0];

    //Extension of the picture files
    myExt = myParameters[1];

    //Concatenation of all key parameters ready for transmission to sub-macros
    ARGcommon = myParameters[2];

    //Time finger prints
    FP = myParameters[3];
    FPT = myParameters[4];

    //Root folder of the analysis
    PathFolderInput = myParameters[5];

    //Selection type:
    //              Whole tissue -/+ manual sub-selection
    //              Manual sub-selection only
    myChoice = myParameters[6];

    //Minimal number of new particles to continue iterations
    minNew = parseFloat(myParameters[7]);

    //Number of bins for the distributions
    nBins = parseFloat(myParameters[8]);

    /*
    ============================================================================
                            IDENTIFICATION OF THE FILES
    ============================================================================
    */

    //Create text Files
    //Listing of files to analyze
    myAnalysis = PathFolderInput + FP + "_Files.txt";
    Listing = File.open(myAnalysis);
    File.close(Listing);

    //Listing of commands
    myCommands = PathFolderInput + FP + "_Parameters.txt";
    Listing = File.open(myCommands);
    File.close(Listing);

    //Launch Files Identification
    ArgFiles = myExt + "*";
    ArgFiles += PathFolderInput + "*";
    ArgFiles += myAnalysis + "*";
    ArgFiles += myCommands + "*";
    ArgFiles += myReuse;

    runMacro(PathIdent, ArgFiles);

    /*
    ============================================================================
                                SELECT THE CHANNEL
    ============================================================================
    */

    //Launch Channel Selection
    ArgChannel = myAnalysis + "*";
    ArgChannel += myReuse;
    myChannel = runMacro(PathSelChannel, ArgChannel);

    /*
    ============================================================================
                            LOOP OF PARAMETERS CREATION
    ============================================================================
    */

    //Launch Manual Taylor on all identified files
    ArgTaylor = myReuse + "\n";
    ArgTaylor += myAnalysis + "\n";
    ArgTaylor += myCommands + "\n";
    ArgTaylor += myChoice + "\n";
    ArgTaylor += PathFolderInput + "\n";
    ArgTaylor += FP + "\n";
    ArgTaylor += FPT + "\n";
    ArgTaylor += "" + minNew + "\n";
    ArgTaylor += myChannel + "\n";
    ArgTaylor += ARGcommon + "\n";
    runMacro(PathTaylor, ArgTaylor);

    //Inform user
    DisplayInfo("Press <b>OK</b> when ready for automated analysis.");

    /*
    ============================================================================
                            LOOP OF ANALYSIS
    ============================================================================
    */

    //Get Beginning Time
    getDateAndTime(year,
                    month,
                    dayOfWeek,
                    dayOfMonth,
                    hour,
                    minute,
                    second,
                    msec);
    T1 = "" + year + "/" + (month+1) + "/" + dayOfMonth + " at ";
    T1 += "" + hour + ":" + minute;

    //Retrieve Commands
    Commands = File.openAsString(myCommands);
    CommandsList = split(Commands, "\n");

    for (c=0; c<CommandsList.length; c++){
        //Use the correct concatenated arguments
        ARG = CommandsList[c];

        //Run Lipid_Droplets
        setBatchMode(true);
        runMacro(PathLD, ARG);
    }

    /*
    ============================================================================
                            STATISTICAL ANALYSIS
    ============================================================================
    */

    //Lauch statistics
    ARGMS = PathFolderInput + "*";
    ARGMS += myAnalysis + "*";
    ARGMS += FP + "*";
    ARGMS += "" + nBins + "*";
    runMacro(PathMS, ARGMS);

    //Get Ending Time
    getDateAndTime(year,
                    month,
                    dayOfWeek,
                    dayOfMonth,
                    hour,
                    minute,
                    second,
                    msec);
    T2 = "" + year + "/" + (month+1) + "/" + dayOfMonth + " at ";
    T2 += "" + hour + ":" + minute;

    EndProcess(T1,T2);

/*
===============================================================================
                            FUNCTIONS
===============================================================================
*/

function Welcome(myTag, myCommit, myQCl, myQCp, myQCm, url){

    //Display a Welcome message to the user.

    showMessage("WELCOME", "<html>"
			+"<font size=+3>"
			+"<h1><font color=rgb(77,172,174)>Automated Detection of labeled particles <br> in microscopic stacks of Drosophila M. tissues</h1>"
			+"<font size=+0>"
			+"<font color=rgb(0,0,0)>"
			+"<ul>"
			+"<li>"
            +"<font color=rgb(0,0,0)>"
            +"Version: "
            +"</font>"
            +"<font color=rgb(77,172,174)>"
            +"<b>"
            + myTag
            +"</b>"
            +"</font>"
            +"</li>"

            +"<li>"
            +"<font color=rgb(0,0,0)>"
            +"Last stable commit: "
            +"</font>"
            +"<font color=rgb(77,172,174)>"
            +"<b>"
            + myCommit
            +"</b>"
            +"</font>"
            +"</li>"

            +"<li>"
            +"<font color=rgb(0,0,0)>"
            +"Quality control LINUX: "
            +"</font>"
            +"<font color=rgb(77,172,174)>"
            +"<b>"
            + myQCl
            +"</b>"
            +"</font>"
            +"</li>"

            +"<li>"
            +"<font color=rgb(0,0,0)>"
            +"Quality control WINDOWS: "
            +"</font>"
            +"<font color=rgb(77,172,174)>"
            +"<b>"
            + myQCp
            +"</b>"
            +"</font>"
            +"</li>"

            +"<li>"
            +"<font color=rgb(0,0,0)>"
            +"Quality control APPLE: "
            +"</font>"
            +"<font color=rgb(77,172,174)>"
            +"<b>"
            + myQCm
            +"</b>"
            +"</font>"
            +"</li>"

			+"</ul>"
			+"<p><font color=rgb(100,100,100)><b>Cluet David</b><br>"
            +"Research Ingeneer,PHD<br>"
            +"<font color=rgb(77,172,174)><b>CNRS, ENS-Lyon, LBMC</b></p>"
			);
}//END WELCOME

/*
================================================================================
*/

function EndProcess(Time1, Time2){

    //Display a message that the Analysis is over.

    showMessage("", "<html>"
			+"<font size=+3>"
			+"<h1><font color=rgb(77,172,174)>Automated Detection of labeled particles <br> in microscopic stacks of Drosophila M. tissues</h1>"
			+"<font size=+0>"
			+"<font color=rgb(0,0,0)>"
            +"<p>Analysis is over.</p>"
			+"<ul>"
			+"<li>Beginning: " + Time1 + "</li>"
			+"<li>End: " + Time2 + "</li>"
			+"</ul>"
			);
}//END ENDPROCESS

/*
================================================================================
*/

function DisplayInfo(Message){

    //Display a message.

    showMessage("", "<html>"
			+"<font size=+3>"
			+"<h1><font color=rgb(77,172,174)>Automated Detection of labeled particles <br> in microscopic stacks of Drosophila M. tissues</h1>"
			+"<font size=+0>"
			+"<font color=rgb(0,0,0)>"
            +"<p>" + Message + "</p>"
			);
}//END DisplayInfo

}//END MACRO
