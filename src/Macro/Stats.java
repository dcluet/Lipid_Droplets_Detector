macro "Stats"{

    Argument = getArgument();
    Arguments = split(Argument, "*");

    myRoot = Arguments[0];
    PathFiles = Arguments[1];
    FP = Arguments[2];
    nBins = parseFloat(Arguments[3]);

    //Open MD
    MD = File.openAsString(myRoot + FP + "GLOBAL_REPORT.md")

    RawListing = File.openAsString(PathFiles);
    ListFiles = split(RawListing, "\n");

    ListStats = newArray("_Values_ALL",
                        "_Corrected_Values_ALL",
                        "_Intensities_ALL",
                        "_Values_NP",
                        "_Corrected_Values_NP",
                        "_Intensities_NP",
                        "_Values_Non-NP",
                        "_Corrected_Values_Non-NP",
                        "_Intensities_Non-NP");
    ListColors = newArray("magenta",
                            "magenta",
                            "magenta",
                            "cyan",
                            "cyan",
                            "cyan",
                            "orange",
                            "orange",
                            "orange");
    ListX = newArray("Droplet (microns)",
                        "Droplet size per million microns Brain",
                        "Mean grey values",
                        "Droplet (microns)",
                        "Droplet size per million microns Brain",
                        "Mean grey values",
                        "Droplet (microns)",
                        "Droplet size per million microns Brain",
                        "Mean grey values")
    Keyword = newArray("DISTRAWJPG",
                        "DISTJPG",
                        "DISTIJPG",
                        "DISTNPRAWJPG",
                        "DISTNPJPG",
                        "DISTNPIJPG",
                        "DISTNNPRAWJPG",
                        "DISTNNPJPG",
                        "DISTNNPIJPG");
    Extension = "_Distribution.csv";

    /*
    ============================================================================
                            LOOP ON DISTRIBUTIONS
    ============================================================================
    */

    for(d=0; d<ListStats.length; d++){

        arrayBins = newArray(nBins);
        arrayValues = newArray(nBins);
        arrayMeans = newArray(nBins);
        arraySEMs = newArray(nBins);

        myHeader = "BINS" + "\t";

        myExt = ListStats[d] + Extension;
        myFiles = "";

        //Loop on every stack
        for(s=0; s<ListFiles.length; s++){
            //open the corresponding CSV file
            Path = ListFiles[s];
            Parent = File.getParent(Path) + File.separator();
            NameFile = File.getName(Path);
            NameFile = substring(NameFile,
                                    0,
                                    lastIndexOf(NameFile, ".")
                                    );
            PathOutput = Parent + NameFile + File.separator();
            PathCSV = PathOutput + NameFile + myExt;

            //Update Header
            myHeader += NameFile + "\t";

            //Update variable for Global REPORT
            if (s==0){
                myFiles += "- " + NameFile + "\n";
            }

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
        File.makeDirectory(PathResultsFolder);
        PathCSV = PathResultsFolder + FP + "Stats" + myExt;
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

        T = getTitle();
        W = getWidth();
        H = getHeight();

        makeRectangle(0,0,W,H);
        run("Copy");
        newImage("Untitled", "RGB white", W, H, 1);
        run("Paste");
        saveAs("Jpeg",
                PathResultsFolder + FP + "Stats_" + ListStats[d] + "_Distribution.jpg");
        close();
        selectWindow(T);
        close();

        MD = replace(MD,
                    Keyword[d],
                    PathResultsFolder + FP + "Stats_" + ListStats[d] + "_Distribution.jpg");

    }

    MD = replace(MD, "MYFILES", "" + myFiles); //OK
    File.saveString(MD, myRoot + FP + "GLOBAL_REPORT.md");

/*
===============================================================================
                            FUNCTIONS
===============================================================================
*/

function startIndex(RawCSV,keyword){
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
