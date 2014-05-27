package es.ramondin.compdec.grafico.view.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import oracle.adf.view.faces.bi.component.graph.GraphFont;
import oracle.adf.view.faces.bi.component.graph.GraphFootnote;
import oracle.adf.view.faces.bi.component.graph.GraphSubtitle;
import oracle.adf.view.faces.bi.component.graph.GraphTitle;
import oracle.adf.view.faces.bi.component.graph.LegendText;
import oracle.adf.view.faces.bi.component.graph.MarkerText;
import oracle.adf.view.faces.bi.component.graph.O1TickLabel;
import oracle.adf.view.faces.bi.component.graph.UIGraph;
import oracle.adf.view.faces.bi.component.graph.X1TickLabel;
import oracle.adf.view.faces.bi.component.graph.Y1TickLabel;
import oracle.adf.view.faces.bi.component.graph.Y2TickLabel;

import org.apache.commons.io.IOUtils;


public class GraficoUtil {
    public static final Integer FORMATO_SALIDA_PDF = 0;
    public static final Integer FORMATO_SALIDA_PNG = 1;
    
    public GraficoUtil() {
        super();
    }

    public static String generarFicheroExportar(FacesContext facesContext, UIGraph comboGraph, String idCompDec, Integer printWidth, Integer printHeight,
                                                String printRutaDestino, String rutaFicheroXSL, String textoEsquinaSupIzq, String textoEsquinaSupDer,
                                                String textoEsquinaInfIzq, String textoEsquinaInfDer, Integer formatoSalida) {
        //Ajustamos los tamaños de los textos
        String inLineStyleComp = comboGraph.getInlineStyle();

        String inlineStyleParams[] = inLineStyleComp.split(";");
        Integer width = null;
        Integer height = null;

        for (int i = 0; i < inlineStyleParams.length; i++) {
            if (inlineStyleParams[i].toUpperCase().startsWith("width".toUpperCase()))
                width = Integer.valueOf(inlineStyleParams[i].substring(inlineStyleParams[i].indexOf(":") + 1).replace("px", ""));
            else if (inlineStyleParams[i].toUpperCase().startsWith("height".toUpperCase()))
                height = Integer.valueOf(inlineStyleParams[i].substring(inlineStyleParams[i].indexOf(":") + 1).replace("px", ""));

            if (width != null && height != null)
                break;
        }

        Double factorConversion = null;
        if (printWidth != null && width != null && printHeight != null && height != null)
            factorConversion = (printWidth * 1.0 / width + printHeight * 1.0 / height) / 2.0;

        final int fontSizeDef = 9;

        Integer fontSizeMarkerText = null, fontSizeGraphTitle = null, fontSizeGraphSubtitle = null, fontSizeGraphFootnote = null, fontSizeX1TickLabel =
            null, fontSizeO1TickLabel = null, fontSizeY1TickLabel = null, fontSizeY2TickLabel = null, fontSizeLegendText = null;

        if (factorConversion != null) {
            MarkerText markerText = comboGraph.getMarkerText();
            GraphFont font = markerText.getGraphFont();
            fontSizeMarkerText = font.getSize();
            font.setSize(((Double)Math.floor((font.getSize() != 0 ? font.getSize() : fontSizeDef) * factorConversion)).intValue());

            //Títlo y subtítulo no funcionan igual, no sé porqué

            GraphTitle graphTitle = comboGraph.getGraphTitle();
            font = graphTitle.getGraphFont();
            fontSizeGraphTitle = font.getSize();
            font.setSize(((Double)Math.floor(fontSizeDef * factorConversion)).intValue());

            GraphSubtitle graphSubtitle = comboGraph.getGraphSubtitle();
            font = graphSubtitle.getGraphFont();
            fontSizeGraphSubtitle = font.getSize();
            font.setSize(((Double)Math.floor(fontSizeDef * factorConversion)).intValue());

            GraphFootnote footnote = comboGraph.getGraphFootnote();
            font = footnote.getGraphFont();
            fontSizeGraphFootnote = font.getSize();
            font.setSize(((Double)Math.floor((font.getSize() != 0 ? font.getSize() : fontSizeDef) * factorConversion)).intValue());

            X1TickLabel x1TickLabel = comboGraph.getX1TickLabel();
            font = x1TickLabel.getGraphFont();
            fontSizeX1TickLabel = font.getSize();
            font.setSize(((Double)Math.floor((font.getSize() != 0 ? font.getSize() : fontSizeDef) * factorConversion)).intValue());

            O1TickLabel o1TickLabel = comboGraph.getO1TickLabel();
            font = o1TickLabel.getGraphFont();
            fontSizeO1TickLabel = font.getSize();
            font.setSize(((Double)Math.floor((font.getSize() != 0 ? font.getSize() : fontSizeDef) * factorConversion)).intValue());

            Y1TickLabel y1TickLabel = comboGraph.getY1TickLabel();
            font = y1TickLabel.getGraphFont();
            fontSizeY1TickLabel = font.getSize();
            font.setSize(((Double)Math.floor((font.getSize() != 0 ? font.getSize() : fontSizeDef) * factorConversion)).intValue());

            Y2TickLabel y2TickLabel = comboGraph.getY2TickLabel();
            font = y2TickLabel.getGraphFont();
            fontSizeY2TickLabel = font.getSize();
            font.setSize(((Double)Math.floor((font.getSize() != 0 ? font.getSize() : fontSizeDef) * factorConversion)).intValue());

            LegendText legendText = comboGraph.getLegendText();
            font = legendText.getGraphFont();
            fontSizeLegendText = font.getSize();
            font.setSize(((Double)Math.floor((font.getSize() != 0 ? font.getSize() : fontSizeDef) * factorConversion)).intValue());
        }

        UIViewRoot root = facesContext.getViewRoot();
        DvtContextCallBack dvtCCB =
            new DvtContextCallBack(comboGraph, printWidth, printHeight, printRutaDestino, rutaFicheroXSL, textoEsquinaSupIzq, textoEsquinaSupDer,
                                   textoEsquinaInfIzq, textoEsquinaInfDer, formatoSalida);
        root.invokeOnComponent(facesContext, idCompDec, dvtCCB);

        //Reestablecemos los tamaños
        if (factorConversion != null) {
            MarkerText markerText = comboGraph.getMarkerText();
            GraphFont font = markerText.getGraphFont();
            font.setSize(fontSizeMarkerText);

            GraphTitle graphTitle = comboGraph.getGraphTitle();
            font = graphTitle.getGraphFont();
            font.setSize(fontSizeGraphTitle);

            GraphSubtitle graphSubtitle = comboGraph.getGraphSubtitle();
            font = graphSubtitle.getGraphFont();
            font.setSize(fontSizeGraphSubtitle);

            GraphFootnote footnote = comboGraph.getGraphFootnote();
            font = footnote.getGraphFont();
            font.setSize(fontSizeGraphFootnote);

            X1TickLabel x1TickLabel = comboGraph.getX1TickLabel();
            font = x1TickLabel.getGraphFont();
            font.setSize(fontSizeX1TickLabel);

            O1TickLabel o1TickLabel = comboGraph.getO1TickLabel();
            font = o1TickLabel.getGraphFont();
            font.setSize(fontSizeO1TickLabel);

            Y1TickLabel y1TickLabel = comboGraph.getY1TickLabel();
            font = y1TickLabel.getGraphFont();
            font.setSize(fontSizeY1TickLabel);

            Y2TickLabel y2TickLabel = comboGraph.getY2TickLabel();
            font = y2TickLabel.getGraphFont();
            font.setSize(fontSizeY2TickLabel);

            LegendText legendText = comboGraph.getLegendText();
            font = legendText.getGraphFont();
            font.setSize(fontSizeLegendText);
        }

        return dvtCCB.getFicheroGenerado();
    }

    public static void exportarFichero(String ficheroGenerado, OutputStream outputStream) throws FileNotFoundException, IOException {
        if (ficheroGenerado != null) {
            File fileGenerado = new File(ficheroGenerado);
            FileInputStream fis = new FileInputStream(fileGenerado);

            IOUtils.copy(fis, outputStream);
            fis.close();
            outputStream.flush();

            //Si todo ha ido bien, podemos borrar el fichero generado
            fileGenerado.delete();
        }
    }
}
