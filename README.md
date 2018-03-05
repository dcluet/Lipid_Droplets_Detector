Automated Detection of Lipid Droplets in *Drosophila M.* brain
===


**Batch analysis version is available**
--

2018/03/02



|![Example original](doc/Original.jpg)|![Example treated](doc/Treated.jpg)|![Example distribution](doc/Distribution.jpg)|![Example corrected distribution](doc/CDistribution.jpg)
|-------------------------------------|-----------------------------------|-----------------------------------|-----------------------------------|
|**ORIGINAL**   |**TREATED**   |**DISTRIBUTION**   |**STATISTICS ENABLED**   |


**On progress**
--
2018/03/05:
- Required corrections and new options added.
- Validated on IJ 1.51t.

Meeting 2018/03/02:

- Implementation of a timer (Initial time only). **Done 2018/03/05**. I have added also a progress corresponding to the stacks already done.
- Fix bug with MYSTART-MYEND. **Done 2018/03/05**.
- Fix the Zdistance (slice and not micron). **Done 2018/03/05**.
- Add 2 for microns square in the GUI. **Done 2018/03/05**.
- Precise "Maximal" for number of iterations in the GUI. **Done 2018/03/05**.
- Debug listing in the Global Report File. **Done 2018/03/05**.
- Add Hyperlink to report of each stack in the global report. **Done 2018/03/05**.
- Add % of total LD / Brain slice or Neuropil into report. **Done 2018/03/05**



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
