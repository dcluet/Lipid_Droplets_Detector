macro "Stats"{

    //Deactivate Display
    setBatchMode(true);

    //Retrieve the arguments into an array
    Argument = getArgument();
    Arguments = split(Argument, "*");

    //Root folder of the analysis
    myRoot = Arguments[0];

    //Name of the file containing the path of the analyzed files
    PathFiles = Arguments[1];

    //Time finger print
    FP = Arguments[2];

    //Number of bins for the distributions
    nBins = parseFloat(Arguments[3]);

    //Open the MarkDown statistic report file as a string
    MD = File.openAsString(myRoot + FP + "GLOBAL_REPORT.md")

    //Get the path of the files
    RawListing = File.openAsString(PathFiles);
    ListFiles = split(RawListing, "\n");

    //Array of the different distribution to perform
    ListStats = newArray("_Values_ALL",
                        "_Corrected_Values_ALL",
                        "_Intensities_ALL",
                        "_Values_NP",
                        "_Corrected_Values_NP",
                        "_Intensities_NP",
                        "_Values_Non-NP",
                        "_Corrected_Values_Non-NP",
                        "_Intensities_Non-NP");

    //Array of the color of each curves
    ListColors = newArray("magenta",
                            "magenta",
                            "magenta",
                            "cyan",
                            "cyan",
                            "cyan",
                            "orange",
                            "orange",
                            "orange");

    //Array of the X axis labels
    ListX = newArray("Droplet (microns)",
                        "Droplet size per million microns Brain",
                        "Mean grey values",
                        "Droplet (microns)",
                        "Droplet size per million microns Brain",
                        "Mean grey values",
                        "Droplet (microns)",
                        "Droplet size per million microns Brain",
                        "Mean grey values");

    //array of the keywords to be replaced in the MD report file
    Keyword = newArray("DISTRAWJPG",
                        "DISTJPG",
                        "DISTIJPG",
                        "DISTNPRAWJPG",
                        "DISTNPJPG",
                        "DISTNPIJPG",
                        "DISTNNPRAWJPG",
                        "DISTNNPJPG",
                        "DISTNNPIJPG");

    //Suffix of the files containing the pre-calculated distributions and bins
    Extension = "_Distribution.csv";

    /*
    ============================================================================
                            LOOP ON DISTRIBUTIONS
    ============================================================================
    */

    //For each type of distribution
    for(d=0; d<ListStats.length; d++){

        //Initialize the arrays that will contain the values
        arrayBins = newArray(nBins);
        arrayValues = newArray(nBins);
        arrayMeans = newArray(nBins);
        arraySEMs = newArray(nBins);

        //Header of the csv file
        myHeader = "BINS" + "\t";

        //Suffix of all csv files required for this specific distribution
        myExt = ListStats[d] + Extension;
        myFiles = "";

        //Loop on every stack
        for(s=0; s<ListFiles.length; s++){
            //open the corresponding CSV file
            Path = ListFiles[s];
            Parent = File.getParent(Path) + File.separator;

            //Get the name without extension of the file
            NameFile = File.getName(Path);
            NameFile = substring(NameFile,
                                    0,
                                    lastIndexOf(NameFile, ".")
                                    );

            //Path of the csv file containing the wanted data for:
            //-the file
            //-the current distribution type
            PathOutput = Parent + FP + "_" + NameFile + File.separator;
            PathCSV = PathOutput + NameFile + myExt;

            //Update Header
            myHeader += NameFile + "\t";

            //Update variable for Global REPORT
            /*
                MD synthax
                [I'm an inline-style link with title](https://www.google.com "Google's Homepage")
            */
            PathFileMD = myRoot + NameFile + "_REPORT.md";
            myFiles += "- [**" + NameFile + "**]";
            myFiles += "(" + PathFileMD + ")" + "\n";

            //Open the csv of the file and retrieve data
            currentCSVf = File.openAsString(PathCSV);
            currentCSV = split(currentCSVf, "\n");

            //Identifies valid lines
            indexS = startIndex(currentCSVf,"BIN") + 1;

            //Retrieve values
            currentBin = 0;
            for(l = indexS; l<currentCSV.length; l++){

                currentLine = currentCSV[l];
                currentvalues = split(currentLine, "\t");

                if (s==0){
                    //First file is used to initialize the arrays
                    arrayBins[currentBin] = parseFloat(currentvalues[0]);
                    arrayValues[currentBin] = "" + currentvalues[1] + "\t";
                } else {
                    //The other files are used to update
                    arrayValues[currentBin] = "" + arrayValues[currentBin]
                                                 + currentvalues[1] + "\t";
                }

                //Get Ready for next bin
                currentBin += 1;
            }

            //Making stats
            for(b=0; b<arrayBins.length; b++){
                currentvaluesString = split(arrayValues[b], "\t");
                currentValues = newArray(currentvaluesString.length);

                //Float Conversion
                for(i=0; i<currentvaluesString.length; i++){
                    currentValues[i] = parseFloat(currentvaluesString[i]);
                }
                Array.getStatistics(currentValues, min, max, mean, stdDev);
                arrayMeans[b] = mean;
                arraySEMs[b] = stdDev/sqrt(currentValues.length);
            }

        }//END Loop on every stack

        //Path CSV
        PathResultsFolder=myRoot + FP + "Stats" + File.separator;
        PathResultsFolderRelative = FP + "Stats" + "/";

        //Create the folder that will contain all the statistical analyses
        File.makeDirectory(PathResultsFolder);

        //Path of the csv file for the current distribution
        PathCSV = PathResultsFolder + FP + "Stats" + myExt;

        //Initiate the file for subsequent append command
        myf = File.open(PathCSV);
        File.close(myf);

        myHeader += "" + "\t" + "MEAN" + "\t" + "SEM";
        File.append(myHeader, PathCSV);

        //Feed the values
        for(b=0; b<arrayBins.length; b++){
            myline = "" + arrayBins[b] + "\t";
            myline += "" + arrayValues[b] + "\t";
            myline += "" + arrayMeans[b] + "\t";
            myline += "" + arraySEMs[b];
            File.append(myline, PathCSV);
        }

        //PLOT CREATION
        Plot.create("Distribution",
                    ListX[d],
                    "Counts");
        Plot.setFrameSize(1000, 500);
        Plot.setColor("black");
        Plot.setLineWidth(4);
        Plot.add("circles", arrayBins, arrayMeans);
        Plot.add("error bars", arraySEMs);
        Plot.setColor(ListColors[d]);
        Plot.setLineWidth(2);
        Plot.add("lines", arrayBins, arrayMeans);
        Plot.show();

        //Get properties of the output graph
        T = getTitle();
        W = getWidth();
        H = getHeight();

        //Generate a JPEG file of the graph
        makeRectangle(0,0,W,H);
        run("Copy");
        newImage("Untitled", "RGB white", W, H, 1);
        run("Paste");
        saveAs("Jpeg",
                PathResultsFolder + FP + "Stats_" + ListStats[d] + "_Distribution.jpg");
        close();
        selectWindow(T);
        close();

        //Update the MarkDown report
        MD = replace(MD,
                    Keyword[d],
                    PathResultsFolderRelative + FP + "Stats_" + ListStats[d] + "_Distribution.jpg");

    }

    //Indicate in the MarkDown report file which files were used for
    //the statistical analysis
    MD = replace(MD, "MYFILES", "" + myFiles);

    //Save the MarkDown File
    File.saveString(MD, myRoot + FP + "GLOBAL_REPORT.md");

/*
===============================================================================
                            FUNCTIONS
===============================================================================
*/

function startIndex(RawCSV,keyword){

    //Give the correct line presenting keyword in the csv
    lines = split(RawCSV, "\n");
    res = -1;

    for(l=0; l<lines.length; l++){
        if(startsWith(lines[l],keyword)){
            res = l;
            l = lines.length * 10; //break
        }
    }
    return res

}//END StartIndex


}//END MACRO
