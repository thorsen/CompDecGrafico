package es.ramondin.compdec.grafico.view.beans;

import es.ramondin.compdec.grafico.view.util.GraficoUtil;
import es.ramondin.util.general.RmdColor;
import es.ramondin.util.general.RmdMensaje;
import es.ramondin.utilidades.JSFUtils;

import java.awt.Color;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import oracle.adf.view.faces.bi.component.graph.ReferenceObject;
import oracle.adf.view.faces.bi.component.graph.ReferenceObjectSet;
import oracle.adf.view.faces.bi.component.graph.UIGraph;
import oracle.adf.view.faces.bi.component.imageView.GradientStopStyle;
import oracle.adf.view.faces.bi.component.imageView.SpecialEffects;
import oracle.adf.view.rich.component.fragment.UIXDeclarativeComponent;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.dss.dataView.DataviewConstants;
import oracle.dss.graph.BaseGraphComponent;
import oracle.dss.graph.GraphConstants;


/**
 * Clase para la gestión interna de un componente declarativo para gráficos
 */
public class GraficoBean {
    private UIGraph comboGraph;

    private static final String[] TEXTO_MARKERS_DEF = new String[] { "No Aceptable", "Norma", "Óptimo" };
    private static final String[] COLOR_MARKERS_DEF = new String[] { "#ff0000", "#0000ff", "#00ff00" };

    /**
     * Constructor
     */
    public GraficoBean() {
    }

    /**
     * Método que añade al mapa pasado los markers pasados como atributos al componente declarativo.<br>
     * Si no se ha pasado ninguno se mostrarán los markers por defecto
     */
    private void cargaMarkers(Map referenceObjectMap) {
        String[] textoMarkers = (String[])JSFUtils.resolveExpression("#{attrs.arrayTextoMarkers}");
        Double[] valorMarkers = (Double[])JSFUtils.resolveExpression("#{attrs.arrayValorMarkers}");
        String[] colorMarkers = (String[])JSFUtils.resolveExpression("#{attrs.arrayColorMarkers}");
        Boolean[] displayMarkers = (Boolean[])JSFUtils.resolveExpression("#{attrs.arrayDisplayMarkers}");

        Color color;
        String texto;
        Boolean display;

        if (valorMarkers != null) {
            for (int i = 0; i < valorMarkers.length; i++) {
                color =
                        colorMarkers != null && colorMarkers.length - 1 >= i ? RmdColor.decode(colorMarkers[i]) : (COLOR_MARKERS_DEF.length - 1 >= i ? RmdColor.decode(COLOR_MARKERS_DEF[i]) :
                                                                                                                  null);
                texto =
                        textoMarkers != null && textoMarkers.length - 1 >= i ? textoMarkers[i] : (TEXTO_MARKERS_DEF.length - 1 >= i ? TEXTO_MARKERS_DEF[i] : null);
                display = displayMarkers != null && displayMarkers.length - 1 >= i ? displayMarkers[i] : (texto != null ? true : false);

                anadeMarker(referenceObjectMap, color, texto, valorMarkers[i], display);
            }
        }
    }

    /**
     * Añade un nuevo marker a la lista de markers pasada como parámetro en <b>referenceObjectMap</b>
     * @param referenceObjectMap Lista de markers existente. Debe ser no nula.
     * @param color Color que tendrá el marker.
     * @param texto Texto que aparecerá en la leyenda en caso de mostrarse.
     * @param valor Valor del eje Y1 en el que se situará el marker. Si es nulo no se añadirá el marker
     * @param display Mostrar en leyenda
     */
    private void anadeMarker(Map referenceObjectMap, Color color, String texto, Double valor, Boolean display) {
        if (valor != null) {
            ReferenceObject referenceObject = new ReferenceObject();

            referenceObject.setIndex(referenceObjectMap.size());
            referenceObject.setType(BaseGraphComponent.RO_LINE);
            referenceObject.setAssociation(GraphConstants.SERIES);
            referenceObject.setLocation("RO_FRONT");

            if (color != null)
                referenceObject.setColor(color);

            referenceObject.setLineStyle("LS_DASH_DOT");
            referenceObject.setLineWidth(2);
            referenceObject.setDisplayedInLegend(display);
            referenceObject.setText(texto);
            referenceObject.setLineValue(valor);

            referenceObjectMap.put(referenceObjectMap.size(), referenceObject);
        }
    }

    /**
     * Método que estable el marker de área para conocer en qué sentido es mejor el valor del dato
     */
    private void anadeMarkerSentidoMejor(Map referenceObjectMap, Integer sentidoMejor) {
        final double STOP_POSITIONS[] = new double[] { 0.0, 5.0, 10.0, 90.0, 95.0, 100.0 };
        final String STOP_COLORS[] = new String[] { "#884add4a", "#444aff4a", "#00ffffff", "#00ffffff", "#44f04c4c", "#88f04c4c" };

        ReferenceObject referenceObject = new ReferenceObject();

        referenceObject.setIndex(referenceObjectMap.size());
        referenceObject.setType(BaseGraphComponent.RO_AREA);
        //        referenceObject.setAssociation(GraphConstants.Y1AXIS);
        referenceObject.setLocation("RO_BACK");
        referenceObject.setDisplayedInLegend(false);
        referenceObject.setShortDesc("");

        SpecialEffects specialEffects = new SpecialEffects();

        specialEffects.setFillType(DataviewConstants.FT_GRADIENT);
        specialEffects.setGradientDirection(DataviewConstants.GD_DOWN);
        specialEffects.setGradientNumStops(STOP_POSITIONS.length);

        Map gradientStopStyleMap = new HashMap();

        GradientStopStyle gradientStopStyle;
        int posIni, signo, pos;

        if (sentidoMejor >= 0) {
            posIni = 0;
            signo = 1;
        } else {
            posIni = STOP_POSITIONS.length - 1;
            signo = -1;
        }

        for (int i = 0; i < STOP_POSITIONS.length; i++) {
            gradientStopStyle = new GradientStopStyle();
            gradientStopStyle.setStopIndex(i);
            gradientStopStyle.setGradientStopPosition(STOP_POSITIONS[i]);

            pos = posIni + (signo * i);

            gradientStopStyle.setGradientStopColor(RmdColor.decode(STOP_COLORS[pos]));

            gradientStopStyleMap.put(gradientStopStyleMap.size(), gradientStopStyle);
        }

        specialEffects.setGradientStopStyleMap(gradientStopStyleMap);
        referenceObject.setSpecialEffects(specialEffects);
        referenceObjectMap.put(referenceObjectMap.size(), referenceObject);
    }

    /**
     * Setter del gráfico
     * @param comboGraph Gráfico que se establece
     */
    public void setComboGraph(UIGraph comboGraph) {
        this.comboGraph = comboGraph;

        if (comboGraph != null && comboGraph.getId() != null) {
            inicializarComponente();
        }
    }

    /**
     * Getter del gráfico
     * @return Gráfico del componente declarativo
     */
    public UIGraph getComboGraph() {
        return comboGraph;
    }

    /**
     * Método que inicializa los datos del componente
     */
    private void inicializarComponente() {
        Map referenceObjectMap = null;
        this.comboGraph.setReferenceObjectSet(new ReferenceObjectSet());

        referenceObjectMap = new HashMap();

        //Cargamos el indicador de mejor
        Integer sentidoMejor = (Integer)JSFUtils.resolveExpression("#{attrs.sentidoMejor}");

        if (sentidoMejor != null && sentidoMejor != 0)
            anadeMarkerSentidoMejor(referenceObjectMap, sentidoMejor);

        //Cargamos los markers
        cargaMarkers(referenceObjectMap);

        this.comboGraph.getReferenceObjectSet().setReferenceObjectMap(referenceObjectMap);

        this.comboGraph.setAutoLayout(GraphConstants.AL_NEVER);
        this.comboGraph.doAutoLayout(GraphConstants.RESET_PROPERTIES);

        AdfFacesContext.getCurrentInstance().addPartialTarget(this.comboGraph);

        this.comboGraph.setAutoLayout(GraphConstants.AL_ALWAYS);
        this.comboGraph.doAutoLayout(GraphConstants.RESET_PROPERTIES);

        AdfFacesContext.getCurrentInstance().addPartialTarget(this.comboGraph);
    }

    public void exportFileAction(FacesContext facesContext, OutputStream outputStream) {
        //Una vez generado el fichero lo descargamos
        String ficheroGenerado = generarFicheroDC(facesContext);

        try {
            GraficoUtil.exportarFichero(ficheroGenerado, outputStream);
        } catch (FileNotFoundException e) {
            RmdMensaje.muestraExcepcion(facesContext, e);
        } catch (IOException e) {
            RmdMensaje.muestraExcepcion(facesContext, e);
        }
    }

    public String generateFileAction() {
        String ficheroGenerado = generarFicheroDC(FacesContext.getCurrentInstance());

        //Lo almacenamos en fileName para poder consultarlo desde fuera
        JSFUtils.setExpressionValue("#{attrs.fileName}", ficheroGenerado);

        return null;
    }

    private String generarFicheroDC(FacesContext facesContext) {
        String idCompDec = (String)JSFUtils.resolveExpression("#{attrs.id}");
        Integer printWidth = (Integer)JSFUtils.resolveExpression("#{attrs.printWidth}");
        Integer printHeight = (Integer)JSFUtils.resolveExpression("#{attrs.printHeight}");
        String printRutaDestino = (String)JSFUtils.resolveExpression("#{attrs.printRutaDestino}");
        String rutaFicheroXSL = (String)JSFUtils.resolveExpression("#{attrs.rutaFicheroXSL}");
        String textoEsquinaSupIzq = (String)JSFUtils.resolveExpression("#{attrs.textoEsquinaSupIzq}");
        String textoEsquinaSupDer = (String)JSFUtils.resolveExpression("#{attrs.textoEsquinaSupDer}");
        String textoEsquinaInfIzq = (String)JSFUtils.resolveExpression("#{attrs.textoEsquinaInfIzq}");
        String textoEsquinaInfDer = (String)JSFUtils.resolveExpression("#{attrs.textoEsquinaInfDer}");
        Integer formatoSalida = (Integer)JSFUtils.resolveExpression("#{attrs.formatoSalida}");

        return GraficoUtil.generarFicheroExportar(facesContext, this.comboGraph, idCompDec, printWidth, printHeight, printRutaDestino, rutaFicheroXSL,
                                                  textoEsquinaSupIzq, textoEsquinaSupDer, textoEsquinaInfIzq, textoEsquinaInfDer, formatoSalida);
    }

    public void setCompDecGrafico(UIXDeclarativeComponent component) {
        JSFUtils.setExpressionValue("#{component}", component);
        JSFUtils.setExpressionValue("#{attrs}", component.getAttributes());
    }

    public void inicializaExterno(UIXDeclarativeComponent component, UIGraph graph) {
        setCompDecGrafico(component);
        setComboGraph(graph);
    }
}
