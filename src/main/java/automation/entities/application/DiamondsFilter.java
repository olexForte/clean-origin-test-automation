package automation.entities.application;

import java.util.HashMap;

public class DiamondsFilter {

    static public final String SHAPES_LABEL   = "shape";
    static public final String CUT_FROM_LABEL = "cut_from";
    static public final String CUT_TO_LABEL   = "cut_to";
    static public final String CARAT_FROM_LABEL = "carat_from";
    static public final String CARAT_TO_LABEL   = "carat_to";
    static public final String COLOR_FROM_LABEL   = "color_from";
    static public final String COLOR_TO_LABEL     = "color_to";
    static public final String CLARITY_FROM_LABEL = "clarity_from";
    static public final String CLARITY_TO_LABEL   = "clarity_to";
    static public final String PRICE_FROM_LABEL   = "price_from";
    static public final String PRICE_TO_LABEL     = "price_to";
    static public final String HEARTS_ARROWS_LABEL   = "hearts_arrows";

    Shape[] shapes;

    float priceFrom = 0;
    float priceTo = 100000000f;

    float caratFrom = 0;
    float caratTo = 1000f;

    Cut cutFrom = Cut.NONE;
    Cut cutTo = Cut.IDEAL;

    Color colorFrom = Color.K;
    Color colorTo = Color.D;

    Clarity clarityFrom = Clarity.SI2;
    Clarity clarityTo = Clarity.IF;

    public DiamondsFilter(){

    }

    public DiamondsFilter(HashMap<String,String> filterAsMap){

        String[] allShapesFromMap;
        if(filterAsMap.containsKey(SHAPES_LABEL) && !filterAsMap.get(SHAPES_LABEL).equals("")) {
            allShapesFromMap = filterAsMap.get(SHAPES_LABEL).split(",");
            Shape[] shapes = new Shape[allShapesFromMap.length];
            for (int i = 0; i < allShapesFromMap.length; i++) {
                shapes[i] = Shape.valueOf(allShapesFromMap[i]);
            }
            this.setShapes(shapes);
        }

        if(filterAsMap.containsKey(CARAT_TO_LABEL) && !filterAsMap.get(CARAT_TO_LABEL).equals(""))
            this.setCaratTo(Float.valueOf(filterAsMap.get(CARAT_TO_LABEL)));
        if(filterAsMap.containsKey(CARAT_FROM_LABEL) && !filterAsMap.get(CARAT_FROM_LABEL).equals(""))
            this.setCaratFrom(Float.valueOf(filterAsMap.get(CARAT_FROM_LABEL)));

        if(filterAsMap.containsKey(PRICE_TO_LABEL) && !filterAsMap.get(PRICE_TO_LABEL).equals(""))
            this.setPriceTo(Float.valueOf(filterAsMap.get(PRICE_TO_LABEL)));
        if(filterAsMap.containsKey(PRICE_FROM_LABEL) && !filterAsMap.get(PRICE_FROM_LABEL).equals(""))
            this.setPriceFrom(Float.valueOf(filterAsMap.get(PRICE_FROM_LABEL)));

        if(filterAsMap.containsKey(CLARITY_FROM_LABEL) && !filterAsMap.get(CLARITY_FROM_LABEL).equals(""))
            this.setClarityFrom(Clarity.valueOf(filterAsMap.get(CLARITY_FROM_LABEL)));
        if(filterAsMap.containsKey(CLARITY_TO_LABEL) && !filterAsMap.get(CLARITY_TO_LABEL).equals(""))
            this.setClarityTo(Clarity.valueOf(filterAsMap.get(CLARITY_TO_LABEL)));

        if(filterAsMap.containsKey(COLOR_FROM_LABEL) && !filterAsMap.get(COLOR_FROM_LABEL).equals(""))
            this.setColorFrom(Color.valueOf(filterAsMap.get(COLOR_FROM_LABEL)));
        if(filterAsMap.containsKey(COLOR_TO_LABEL) && !filterAsMap.get(COLOR_TO_LABEL).equals(""))
            this.setColorTo(Color.valueOf(filterAsMap.get(COLOR_TO_LABEL)));

        if(filterAsMap.containsKey(CUT_FROM_LABEL) && !filterAsMap.get(CUT_FROM_LABEL).equals(""))
            this.setCutFrom(Cut.valueOf(filterAsMap.get(CUT_FROM_LABEL).replace(" ", "_")));
        if(filterAsMap.containsKey(CUT_TO_LABEL) && !filterAsMap.get(CUT_TO_LABEL).equals(""))
            this.setCutTo(Cut.valueOf(filterAsMap.get(CUT_TO_LABEL)));

        if(filterAsMap.containsKey(HEARTS_ARROWS_LABEL) && !filterAsMap.get(HEARTS_ARROWS_LABEL).equals(""))
            this.setHeartsArrows(Boolean.parseBoolean(filterAsMap.get(HEARTS_ARROWS_LABEL)));
    }

    public Shape[] getShapes() {
        return shapes;
    }

    public void setShapes(Shape[] shapes) {
        this.shapes = shapes;
    }

    public float getPriceFrom() {
        return priceFrom;
    }

    public void setPriceFrom(float priceFrom) {
        this.priceFrom = priceFrom;
    }

    public float getPriceTo() {
        return priceTo;
    }

    public void setPriceTo(float priceTo) {
        this.priceTo = priceTo;
    }

    public float getCaratFrom() {
        return caratFrom;
    }

    public void setCaratFrom(float cartFrom) {
        this.caratFrom = cartFrom;
    }

    public float getCaratTo() {
        return caratTo;
    }

    public void setCaratTo(float caratTo) {
        this.caratTo = caratTo;
    }

    public Cut getCutFrom() {
        return cutFrom;
    }

    public void setCutFrom(Cut cutFrom) {
        this.cutFrom = cutFrom;
    }

    public Cut getCutTo() {
        return cutTo;
    }

    public void setCutTo(Cut cutTo) {
        this.cutTo = cutTo;
    }

    public Color getColorFrom() {
        return colorFrom;
    }

    public void setColorFrom(Color colorFrom) {
        this.colorFrom = colorFrom;
    }

    public Color getColorTo() {
        return colorTo;
    }

    public void setColorTo(Color colorTo) {
        this.colorTo = colorTo;
    }

    public Clarity getClarityFrom() {
        return clarityFrom;
    }

    public void setClarityFrom(Clarity clarityFrom) {
        this.clarityFrom = clarityFrom;
    }

    public Clarity getClarityTo() {
        return clarityTo;
    }

    public void setClarityTo(Clarity clarityTo) {
        this.clarityTo = clarityTo;
    }

    public boolean isHeartsArrows() {
        return heartsArrows;
    }

    public void setHeartsArrows(boolean heartsArrows) {
        this.heartsArrows = heartsArrows;
    }

    boolean heartsArrows = false;

    public enum Shape{
        ROUND,
        CUSHION,
        HEART,
        OVAL,
        EMERALD,
        PEAR,
        PRINCESS,
        RADIANT,
        ASSCHER,
        MARQUISE
    }

    public enum Cut{
        NONE,
        VERY_GOOD,
        EXCELLENT,
        IDEAL
    }

    public enum Color{
        K, J, I, H, G, F, E, D
    }

    public enum Clarity{
        SI2, SI1, VS2, VS1, VVS2, VVS1, IF
    }


    // TODO move follofing methods to processor class

    public boolean isCaratInFilterRange(String carat){
        Float value = Float.parseFloat(carat);
        return (value <= getCaratTo() && value >= getCaratFrom());
    }

    public boolean isPriceInFilterRange(String price){
        Float value = Float.parseFloat(price);
        return (value <= getPriceTo() && value >= getPriceFrom());
    }

    public boolean isColorInFilterRange(String color) {
        Color value = Color.valueOf(color);
        return (value.ordinal() <= getColorTo().ordinal() && value.ordinal() >= getColorFrom().ordinal());
    }

    public boolean isCutInFilterRange(String cut) {
        Cut value = Cut.valueOf(cut);
        return (value.ordinal() <= getCutTo().ordinal() && value.ordinal() >= getCutFrom().ordinal());
    }

    public boolean isClarityInFilterRange(String clarity) {
        Clarity value = Clarity.valueOf(clarity);
        return (value.ordinal() <= getClarityTo().ordinal() && value.ordinal() >= getClarityFrom().ordinal());
    }

    public boolean isShapesInFilter(String shapes){
        String[] allShapesFromMap = shapes.split(",");
        for (String curItem : allShapesFromMap) {
            boolean result = false;
            if(getShapes() == null)
                return true;
            for(Shape shape : getShapes()){
                if(shape.name().equals(curItem)) {
                    result = true;
                    break;
                }
            }
            if(!result){
                return false;
            }
        }
        return true;
    }

    public boolean isHeartsAndArrows(String heartsArrows){
        return Boolean.parseBoolean(heartsArrows) == isHeartsArrows();
    }

}
//enum.valueOf("VALUE").ordinal()

