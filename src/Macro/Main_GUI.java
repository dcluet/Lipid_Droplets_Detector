macro "Main_GUI"{

    /*
    Parameters structure as saved in .csv:
    ======================================
    */

    Argument = getArgument();
    Arguments = split(Argument, ",");

    //0   Name of the analysis
    myAnalysistype = Arguments[0];

    //1   Extension
    myExt = Arguments[1];

    //2   Reference resoltion (micron/pixel) in X
    ResWref = parseFloat(Arguments[2]);

    //3   Reference resoltion (micron/pixel) in Y
    ResHref = parseFloat(Arguments[3]);

    //4   Distance xy in pixels between 2 particles
    xythresholdMicron = parseFloat(Arguments[4]) * ResWref * ResHref;

    //5   Distance in z between 2 particles
    zthreshold = parseFloat(Arguments[5]);

    //6   Minimum size in microns
    SizeMinMicron = parseFloat(Arguments[6]) * ResWref * ResHref;

    //7   Maximum size in microns
    SizeMaxMicron = parseFloat(Arguments[7]) * ResWref * ResHref;

    //8   Maximum size (to exclude big fat bodies)
    SizeMaxCMicron = parseFloat(Arguments[8]) * ResWref * ResHref;

    //9   Minimum circularity
    CircMinC = parseFloat(Arguments[9]);

    //10   Maximum circularity
    CircMaxC = parseFloat(Arguments[10]);

    //11  Number of Iterations
    nIteration = parseFloat(Arguments[11]);

    //12  Zone for enlargement (in pixel) and erasing
    CorrectionSize = parseFloat(Arguments[12]);

    //13  Number of bins for distributions
    nBins = parseFloat(Arguments[13]);

    //14  Type of the analysis Zone
    myZone = Arguments[14];
    if (myZone == "?"){
        Selections = newArray("Whole tissue with Sub-Selection",
                                "Whole tissue",
                                "Manual ROI");
    }else{
        Selections = newArray(myZone, "");
        Selections = Array.trim(Selections, 1);
    }

    //15  Minimal number of particules to continue iterations
    minNew = parseFloat(Arguments[15]);

    //16 Enhance signal
    enhance = parseFloat(Arguments[16]);

    //Initialisation of the Argument
    ARGcommon = "";

    //GUI
    Dialog.create("SETTINGS FOR " + myAnalysistype);
    Dialog.addMessage("MAIN PARAMETERS:");
    reuse = newArray("NO", "YES");
    Dialog.addChoice("Reload previous taylored parameters", reuse, "NO");
    Dialog.addString("Extension of the stacks files: ", myExt, 5);
    Dialog.addChoice("Region to process: ", Selections);
    Dialog.addMessage("INITIAL RESOLUTION USED FOR CALIBRATION:");
    Dialog.addNumber("Pixel Width ", ResWref, 3, 5, "micron");
    Dialog.addToSameRow();
    Dialog.addNumber("Pixel Height: ", ResHref, 3, 5, "micron");

    Dialog.addMessage("THRESHOLDS BETWEEN ISOLATED PARTICLES:");
    Dialog.addNumber("XY Distance: ", xythresholdMicron, 3, 5, "micron");
    Dialog.addToSameRow();
    Dialog.addNumber("Z Distance: ", zthreshold, 3, 5, "slices");

    Dialog.addMessage("INITIAL LOW RESOLUTION SCAN:");
    Dialog.addNumber("Minimal surface: ", SizeMinMicron, 3, 7, "microns^2");
    Dialog.addToSameRow();
    Dialog.addNumber("Maximal surface: ", SizeMaxMicron, 3, 7, "microns^2");

    Dialog.addMessage("HIGH RESOLUTION SCAN:");
    Dialog.addNumber("Maximal surface: ", SizeMaxCMicron, 3, 7, "microns^2");
    Dialog.addToSameRow();
    Dialog.addNumber("Minimal circularity: ", CircMinC, 3, 5, "");
    Dialog.addToSameRow();
    Dialog.addNumber("Maximal circularity: ", CircMaxC, 3, 5, "");

    Dialog.addMessage("PARAMETERS FOR ITERATION ENGINE:");
    Dialog.addNumber("Number of maximal iterations: ", nIteration);
    Dialog.addNumber("Number of minimal new particle to perform next iteration", minNew);
    Dialog.addNumber("Correction factor: ", CorrectionSize, 0, 1, "pixels");
    Dialog.addNumber("Number of bins for the distributions: ", nBins, 0, 3, "");
    Dialog.addCheckbox("Enhance signal", enhance);
    Dialog.show();

    //Generate the Ergument string for all analyses
    myReuse = Dialog.getChoice();
    myExt = Dialog.getString();
    myChoice = Dialog.getChoice();
    ResWref = Dialog.getNumber();
    ResHref = Dialog.getNumber();
    resoRef = "" + ResWref + " x " + ResHref + " microns";
    ImResolution = ResWref*ResHref;

    ARGcommon += myChoice + "*";
    ARGcommon += "" + ResWref + "*";
    ARGcommon += "" + ResHref + "*";
    xythreshold = Dialog.getNumber() / (ResWref * ResHref);
    ARGcommon += "" + xythreshold + "*";
    zthreshold = Dialog.getNumber();
    ARGcommon  += "" + zthreshold + "*";
    SizeMin = Dialog.getNumber() / (ResWref * ResHref);
    ARGcommon  += "" + SizeMin + "*";
    SizeMax = Dialog.getNumber() / (ResWref * ResHref);
    ARGcommon  += "" + SizeMax + "*";
    SizeMaxC = Dialog.getNumber() / (ResWref * ResHref);
    ARGcommon  += "" + SizeMaxC + "*";
    CircMinC = Dialog.getNumber();
    ARGcommon  += "" + CircMinC + "*";
    CircMaxC = Dialog.getNumber();
    ARGcommon += "" + CircMaxC + "*";
    Iterations = Dialog.getNumber();
    ARGcommon += "" + Iterations + "*";
    minNew = Dialog.getNumber();
    enlargement = Dialog.getNumber();
    ARGcommon += "" + enlargement + "*";
    nBins = "" + Dialog.getNumber();
    ARGcommon += "" + nBins + "*";
    enhance = Dialog.getCheckbox();
    ARGcommon += "" + enhance + "*";
    ARGcommon += "" + myAnalysistype + "*"; //Analysis type

    //Generate Finger Print
    getDateAndTime(year,
                    month,
                    dayOfWeek,
                    dayOfMonth,
                    hour,
                    minute,
                    second,
                    msec);
    FP = "" + year + "-" + (month+1) + "-" + dayOfMonth + "_";
    FP += "" + hour + "-" + minute + "_";

    FPT = "" + year + "/" + (month+1) + "/" + dayOfMonth + " at ";
    FPT += "" + hour + ":" + minute;

    //Retrieve folder to explore
    myTitle = "PLEASE CHOOSE THE FOLDER CONTAINING THE FILES TO PROCESS";
    PathFolderInput = getDirectory(myTitle);

    //Prepare the MarkDown Report File
    fillMD();

    //Return key parameters to main program
    OUTPUT = myReuse + "\n";
    OUTPUT += myExt + "\n";
    OUTPUT += ARGcommon + "\n";
    OUTPUT += FP + "\n";
    OUTPUT += FPT + "\n";
    OUTPUT += PathFolderInput + "\n";
    OUTPUT += myChoice + "\n";
    OUTPUT += "" + minNew + "\n";
    OUTPUT += "" + nBins;

    //Transmit parameters to the main macro
    return OUTPUT;

/*
===============================================================================
                            FUNCTIONS
===============================================================================
*/

function fillMD(){
    //Prepare the markDown Report

    PathMD = getDirectory("macros");
    PathMD += "Droplets"+File.separator;
    PathMD += "Final_report.md";
    MD = File.openAsString(PathMD);
    MD = replace(MD, "MYFP", "" + FPT);
    MD = replace(MD, "MYOS", getInfo("os.name"));
    MD = replace(MD, "MYJAVA", getInfo("java.version"));
    MD = replace(MD, "MYIJ", getVersion());
    MD = replace(MD, "MYSELECTION", myChoice);
    MD = replace(MD, "MYREFERENCE", "" + resoRef);
    MD = replace(MD, "XYTHRESHOLD", "" + (xythreshold* ImResolution) + " microns");
    MD = replace(MD, "ZTHRESHOLD", "" + (zthreshold* ImResolution) + " microns");
    MD = replace(MD, "MYITERATIONS", "" + Iterations);
    MD = replace(MD, "MYFACTOR", "" + enlargement + " pixels");
    MD = replace(MD, "MINSURF", "" + (SizeMin * ImResolution) + " microns");
    MD = replace(MD, "MAXSURF", "" + (SizeMax * ImResolution) + " microns");
    MD = replace(MD, "SURFMAXC", "" + (SizeMaxC * ImResolution) + " microns");
    MD = replace(MD, "MINCIRC", "" + CircMinC);
    MD = replace(MD, "MAXCIRC", "" + CircMaxC);
    if (myAnalysistype == "Repo"){
        MD = replace(MD, "LD", "REPO");
        MD = replace(MD, "droplets", "REPO");
    }

    File.saveString(MD, PathFolderInput + FP + "GLOBAL_REPORT.md");
}//END OF FILL MD

}//END OF MACRO
