macro "Main_GUI"{
    /*
    Parameters structure as saved in .csv:
    ======================================
    0   Name of the analysis
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
    */

    Argument = getArgument();
    Arguments = split(Argument, ",");

    myAnalysistype = Arguments[0];
    myExt = Arguments[1];
    ResWref = parseFloat(Arguments[2]);
    ResHref = parseFloat(Arguments[3]);
    xythresholdMicron = parseFloat(Arguments[4]) * ResWref * ResHref;
    zthreshold = parseFloat(Arguments[5]);
    SizeMinMicron = parseFloat(Arguments[6]) * ResWref * ResHref;
    SizeMaxMicron = parseFloat(Arguments[7]) * ResWref * ResHref;
    SizeMaxCMicron = parseFloat(Arguments[8]) * ResWref * ResHref;
    CircMinC = parseFloat(Arguments[9]);
    CircMaxC = parseFloat(Arguments[10]);
    nIteration = parseFloat(Arguments[11]);
    CorrectionSize = parseFloat(Arguments[12]);
    nBins = parseFloat(Arguments[13]);
    //Region of Analysis
    myZone = Arguments[14];
    if (myZone == "?"){
        Selections = newArray("Whole tissue with Sub-Selection",
                                "Whole tissue",
                                "Manual ROI");
    }else{
        Selections = newArray(myZone, "");
        Selections = Array.trim(Selections, 1);
    }
    minNew = parseFloat(Arguments[15]);

    //Initialisation of the Argument
    ARGcommon = "";

    //GUI
    Dialog.create("SETTINGS FOR " + myAnalysistype);
    Dialog.addMessage("Re-use the same initial, final and manual selection of a previous analysis?");
    reuse = newArray("NO", "YES");
    Dialog.addChoice("", reuse, "NO");
    Dialog.addString("Extension of the stacks files: ", myExt, 5);
    Dialog.addMessage("Initial resolution used for calibration:");
    Dialog.addNumber("Pixel Width ", ResWref, 3, 5, "micron");
    Dialog.addNumber("Pixel Height: ", ResHref, 3, 5, "micron");
    Dialog.addChoice("Region to process: ", Selections);


    Dialog.addMessage("Thresholds between particles:");
    Dialog.addNumber("XY Distance: ", xythresholdMicron, 3, 5, "microns");
    Dialog.addNumber("Z Distance: ", zthreshold, 3, 5, "slices");

    Dialog.addMessage("Parameters for the initial low-resolution scan:");
    Dialog.addNumber("Minimal surface: ", SizeMinMicron, 3, 7, "microns^2");
    Dialog.addNumber("Maximal surface: ", SizeMaxMicron, 3, 7, "microns^2");

    Dialog.addMessage("Parameters for the high-resolution scan:");
    Dialog.addNumber("Maximal surface: ", SizeMaxCMicron, 3, 7, "microns^2");
    Dialog.addNumber("Minimal circularity: ", CircMinC, 3, 5, "");
    Dialog.addNumber("Maximal circularity: ", CircMaxC, 3, 5, "");

    Dialog.addMessage("This program is based on iterative detection of the the brightest particles.");
    Dialog.addNumber("Number of maximal iterations: ", nIteration);
    Dialog.addNumber("Number of minimal new particle to perform next iteration", minNew);
    Dialog.addNumber("Correction factor: ", CorrectionSize, 0, 1, "pixels");
    Dialog.addNumber("Number of bins for the distributions: ", nBins, 0, 3, "");
    Dialog.show();

    myReuse = Dialog.getChoice();

    myExt = Dialog.getString();
    ResWref = Dialog.getNumber();
    ResHref = Dialog.getNumber();
    resoRef = "" + ResWref + " x " + ResHref + " microns";
    ImResolution = ResWref*ResHref;
    myChoice = Dialog.getChoice();
    if (myChoice == "Whole tissue"){
        ARGcommon  += "Brain" + "*";
    }else if (myChoice == "Manual ROI"){
        ARGcommon  += "Manual ROI" + "*";
    }else if (myChoice == "Whole tissue with Sub-Selection"){
        ARGcommon += "BrainNP" + "*";
    }

    ARGcommon  += "" + ResWref + "*";
    ARGcommon  += "" + ResHref + "*";

    xythreshold = Dialog.getNumber() / (ResWref * ResHref);
    ARGcommon  += "" + xythreshold + "*";
    zthreshold = Dialog.getNumber();
    ARGcommon  += "" + zthreshold + "*";

    SizeMin = Dialog.getNumber() / (ResWref * ResHref);
    ARGcommon  += "" + SizeMin + "*";
    SizeMax = Dialog.getNumber() / (ResWref * ResHref);
    ARGcommon  += "" + SizeMax + "*";

    SizeMaxC = Dialog.getNumber() / (ResWref * ResHref);
    ARGcommon  += "" + SizeMaxC + "*";
    CircMinC = Dialog.getNumber();
    ARGcommon  += "" + CircMinC + "*"; //Circ Min
    CircMaxC = Dialog.getNumber();
    ARGcommon  += "" + CircMaxC + "*"; //Circ Maximal
    Iterations = Dialog.getNumber();
    ARGcommon  += "" + Iterations + "*"; //Iterations
    minNew = Dialog.getNumber();
    enlargement = Dialog.getNumber();
    ARGcommon  += "" + enlargement + "*"; //Enlarge
    nBins = "" + Dialog.getNumber(); //Bins
    ARGcommon  += "" + nBins + "*"; //Bins

    ARGcommon  += "" + myAnalysistype + "*"; //Analysis type

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

    return OUTPUT;


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
    }
}
