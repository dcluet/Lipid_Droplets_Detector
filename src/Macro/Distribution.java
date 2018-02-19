macro "Distribution" {
    BinMin = 0;
    BinMax = 1000;
    nBins = 100;

    //Create the bins arrays
    binSize = (BinMax-BinMax)/nBins;
    binBlocks = newArray();
    binValues = newArray();
    for (b=BinMin; b<=BinMax; b+=binSize){
        binBlocks = Array.concat(binBlocks, b);
    }

    for (index=0; index<binBlocks.length-1; index++){
        v = (binBlocks[index] + binBlocks[index+1])/2;
        binValues = Array.concat(binValues, v);
    }

    Array.show(binBlocks);
    Array.show(binValues);

}//END macro
