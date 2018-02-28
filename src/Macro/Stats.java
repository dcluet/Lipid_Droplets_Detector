macro "Stats"{

    Argument = getArgument();
    Arguments = split(Argument, "*");

    myRoot = Arguments[0];
    PathFiles = Arguments[1];
    FP = Arguments[2];

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
    Extension = "_Distribution.csv";

    /*
    ============================================================================
                            LOOP ON DISTRIBUTIONS
    ============================================================================
    */
    for(d=0; d<ListStats.length; d++){
        myExt = ListStats[d] + Extension;

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
            waitForUser(PathCSV);
            currentCSV = File.openAsString(PathCSV);

            //Identifies valid lines
            indexS = startIndex(currentCSV,"BIN") + 1;


        }//END Loop on every stack


    }

/*
===============================================================================
                            FUNCTIONS
===============================================================================
*/

function startIndex(RawCSV,keyword){
    lines = split(RawCSV, "\n");
    res = -1;

    for(l=0; l<lines.length; l++){
        if(startsWith(RawCSV,keyword)){
            waitForUser("Working " + l);
            res = l;
            l = lines.length * 10; //break
        }
    }
    return res

}//END StartIndex


}//END MACRO
