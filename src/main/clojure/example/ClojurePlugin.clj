; We do not implement a PlugInFilter since that would require us to remember
; the ImagePlus parameter of the setup method in order to update the current
; image. Instead, we implement a filter plugin "by hand".

(ns example.ClojurePlugin
  (:import [ij WindowManager]
           [ij ImagePlus]
           [ij.process ImageProcessor])
  (:gen-class :implements [ij.plugin.PlugIn]))

(defn -run [this arg]
  (let [imp (ij.WindowManager/getCurrentImage)
        ip (. imp getProcessor)
        pixels (. ip getPixels)
        width (. ip getWidth)
        height (. ip getHeight)]
    (dotimes [i (int (* width height))]
      (aset pixels i (byte i)))
    (. imp updateAndDraw)))
