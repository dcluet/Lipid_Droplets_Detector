macro "Main"{
    //INFOS
    tag = "v4.1.0"
    lastStableCommit = "09c54a63"
    gitlaburl = "http://gitlab.biologie.ens-lyon.fr/dcluet/Lipid_Droplets"

    /*
    ============================================================================
                        QC TESTS
    ============================================================================

    Linux
    Lipid Droplets Brain (Whole Tissue with Sub-selection): 2018-09-10
    Lipid Droplets Brain (Whole Tissue): 2018-09-10
    Lipid Droplets Brain (Manual ROI): 2018-09-10
    Lipid Droplets Retina (Manual ROI): 2018-09-10
    REPO (Whole Tissue): 2018-09-10


    */

    qcl = "2018-09-10";
    qcp = "On progress";
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

    //Welcome
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

    ResultGUI = runMacro(PathGUI, param);

    //Extract key parameters and command line
    myParameters = split(ResultGUI, "\n");
    myReuse = myParameters[0];
    myExt = myParameters[1];
    ARGcommon = myParameters[2];
    FP = myParameters[3];
    FPT = myParameters[4];
    PathFolderInput = myParameters[5];
    myChoice = myParameters[6];
    minNew = parseFloat(myParameters[7]);
    nBins = parseFloat(myParameters[8]);

    /*
    ============================================================================
                            IDENTIFICATION OF THE FILES
    ============================================================================
    */

    myAnalysis = PathFolderInput + FP + "_Files.txt";
    myCommands = PathFolderInput + FP + "_Parameters.txt";

    //Create text Files
    Listing = File.open(myAnalysis);
    File.close(Listing);
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
    showMessage("WELCOME", "<html>"
			+"<font size=+3>"
			+"<h1><font color=rgb(77,172,174)>Lipid Droplets and REPO Analysis</h1>"
			+"<font size=+0>"
			+"<font color=rgb(0,0,0)>"
			+"<ul>"
			+"<li>Version: " + myTag + "</li>"
			+"<li>Last stable commit: " + myCommit + "</li>"
            +"<li>Quality control LINUX: " + myQCl + "</li>"
            +"<li>Quality control WINDOWS: " + myQCp + "</li>"
            +"<li>Quality control APPLE: " + myQCp + "</li>"
			+"</ul>"
			+"<p><font color=rgb(100,100,100)>Cluet David<br>"
            +"Research Ingeneer,PHD<br>"
            +"<font color=rgb(77,172,174)>CNRS, ENS-Lyon, LBMC</p>"
			);
}//END WELCOME

/*
================================================================================
*/

function EndProcess(Time1, Time2){
    showMessage("", "<html>"
			+"<font size=+3>"
			+"<h1><font color=rgb(77,172,174)>Lipid Droplets Analysis</h1>"
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
    showMessage("", "<html>"
			+"<font size=+3>"
			+"<h1><font color=rgb(77,172,174)>Lipid Droplets Analysis</h1>"
			+"<font size=+0>"
			+"<font color=rgb(0,0,0)>"
            +"<p>" + Message + "</p>"
			);
}//END DisplayInfo

}//END MACRO
