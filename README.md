Automated Detection of Lipid Droplets in *Drosophila M.* brain
===


**Test version available**
--

2018/02/20

- Implement distribution of non NP droplets.
- Creation of csv files for the distributions:
    - All droplets
    - Non Neuropil droplets
    - Neuropil droplets
- Tested and validated on ImageJ and FIJI linux (Java bundle) 1.51t.
- Cleaning of the repository.

**NB: FIJI is 2 to 3 times slower than ImageJ**


|![Example original](doc/Original.jpg)|![Example treated](doc/Treated.jpg)|![Example distribution](doc/Distribution.jpg)|![Example corrected distribution](doc/CDistribution.jpg)
|-------------------------------------|-----------------------------------|-----------------------------------|-----------------------------------|
|**ORIGINAL**   |**TREATED**   |**DISTRIBUTION**   |**CORRECTED DISTRIBUTION**   |


**On progress**
--
- The script for automated batch analysis is ready. It will be added in the final stage, as usual.
- Implementation of the GUI. **Started 2018/02/27**
- The scripts now adapt to the resolution of the current image but with a fixed micron scale range to display the distributions. **Done 2018/02/26**
- Depending on the choice, the analysis is performed on the whole brain or only on the "Neuropil" manual region. **Done 2018/02/26**
- Automated detection of the brain for every slice of the Stack. **Done 2018/02/09**. Still issues to detect holes. Sensitivity has been increased **2018/02/23**.
- Integration within the loop. **Done 2018/02/09**.
- Express the number/size of the droplets particles as a distribution standardized on the area of brain studied. **Done 2018/02/19**
- Surfaces expressed in microns units. **Done 2018/02/26**
- The mean intensity of each droplet is now measured. **Done 2018/02/26**
- Add Distribution for intensities. **Done 2018/02/26**
- Create the distribution graph script. **Done 2018/02/19**
- Create a more "readable" output table. **Done 2018/02/19**
- Final output stack **Done 2018/02/09**:
    - Brain section in Grey.
    - If possible Neuropiles in color.
    - Droplets in white surrounded in purple (detected).
- Use the surface of the central Neuropil as a reference to validate that all brain have been studied in the same conditions?


**Authors**
--

| ![LBMC Logo](doc/Logo_LBMC.jpg) ![CNRS Logo](doc/Logo_cnrs.jpg) ![ENS Logo](doc/Logo_ens.jpg) ||
|-----------------------------|------------|
|**CLUET David**|     [david.cluet@ens-lyon.fr](david.cluet@ens-lyon.fr)|


**Requirements**
--
The `LIPID_DROPLETS` macro requires `ImageJ v1.49g` or higher ([Download](https://imagej.nih.gov/ij/download.html)).

For ImageJ, the conversion of the analyzed stacks into animated GIFs requires the ([Gif-Stack-Writer Plugin](https://imagej.nih.gov/ij/plugins/gif-stack-writer.html)).

To read Markdown files, use the `Markdown Preview Plus` extension for `Chrome`. In the `Extension menu` allow access to files URL.


**Installation**
--
The `LIPID_DROPLETS` macro requires can be automatically installed with all required files in `ImageJ` and `FIJI`. Please follow the specific instructions described below.


![ImageJ Logo](doc/IJ.jpg)
---
1. Open `ImageJ`.
2. Open the `src` folder of the `LIPID_DROPLETS` macro.
3. Drag the `Installation.ijm` file on `ImageJ` Menu bar to open it.
4. In the Menu bar of the macro select the `Macros/Run Macro` option.
5. The window will be closed automatically and all required files will be installed in the `ImageJ/macros/Droplets` folder. The shortcut `Plugins/Macros/LIPID_DROPLETS` will be added in the Menu bar.
6. Restart `ImageJ` to refresh the Menu bar.


![FIJI Logo](doc/FIJI.jpg)
---
1. Open `FIJI`.
2. Open the `src` folder of the `LIPID_DROPLETS` macro.
3. Drag the `Installation_Fiji.ijm` file on `FIJI` Menu bar to open it.
4. In the console select the `Run` option.
5. All required files will be installed in the `Fiji.app/macros/Droplets` folder. The shortcut `Plugins/Macros/LIPID_DROPLETS` will be added in the Menu bar.
6. Restart `FIJI` to refresh the Menu bar.


Update
---
Follow the same instructions as for the installation process.
