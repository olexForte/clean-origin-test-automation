package automation.entities.application;

import automation.tools.ComparatorTool;

import java.util.Arrays;
import java.util.HashMap;

public class RingFilter {

    static public final String METAL_LABEL   = "metal";
    static public final String COLLECTION_LABEL   = "collection";
    static public final String PRICE_FROM_LABEL   = "price_from";
    static public final String PRICE_TO_LABEL     = "price_to";


    RingCollection[] collections;

    public RingCollection[] getCollections() {
        return collections;
    }

    public void setCollections(RingCollection[] collections) {
        this.collections = collections;
    }

    public RingMetal[] getMetals() {
        return metals;
    }

    public void setMetals(RingMetal[] metals) {
        this.metals = metals;
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

    RingMetal[] metals;

    float priceFrom = 0;
    float priceTo = 100000000f;


    public RingFilter(){

    }

    public RingFilter(HashMap<String,String> filterAsMap){

        String[] allMetalsFromMap;
        if(filterAsMap.containsKey(METAL_LABEL) && !filterAsMap.get(METAL_LABEL).equals("")) {
            allMetalsFromMap = filterAsMap.get(METAL_LABEL).split(",");
            RingMetal[] metals = new RingMetal[allMetalsFromMap.length];
            for (int i = 0; i < allMetalsFromMap.length; i++) {
                metals[i] = RingMetal.labelFor(allMetalsFromMap[i]);
            }
            this.setMetals(metals);
        }

        String[] allCollectionsFromMap;
        if(filterAsMap.containsKey(COLLECTION_LABEL) && !filterAsMap.get(COLLECTION_LABEL).equals("")) {
            allCollectionsFromMap = filterAsMap.get(COLLECTION_LABEL).split(",");
            RingCollection[] collections = new RingCollection[allCollectionsFromMap.length];
            for (int i = 0; i < allCollectionsFromMap.length; i++) {
                collections[i] = RingCollection.valueOf(allCollectionsFromMap[i].toUpperCase());
            }
            this.setCollections(collections);
        }

        if(filterAsMap.containsKey(PRICE_TO_LABEL) && !filterAsMap.get(PRICE_TO_LABEL).equals(""))
            this.setPriceTo(Float.valueOf(filterAsMap.get(PRICE_TO_LABEL)));
        if(filterAsMap.containsKey(PRICE_FROM_LABEL) && !filterAsMap.get(PRICE_FROM_LABEL).equals(""))
            this.setPriceFrom(Float.valueOf(filterAsMap.get(PRICE_FROM_LABEL)));

    }


    public enum RingMetal{
        _14K_White_Gold("14K White Gold", "94"),
        _14K_Yellow_Gold("14K Yellow Gold", "95"),
        _14K_Rose_Gold("14K Rose Gold", "96"),
        _18K_White_Gold("18K White Gold", "97"),
        _18K_Yellow_Gold("18K Yellow Gold", "98"),
        _Platinum("Platinum", "99");

        String label;
        String id;

        RingMetal(String value, String id){
            this.label = value;
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        static public RingMetal labelFor(String value){
            for(RingMetal val : values()){
                if(val.getLabel().equals(value))
                    return val;
            }
            return null;
        }

        public String getId() {
            return label;
        }

        static public RingMetal idFor(String value){
            for(RingMetal val : values()){
                if(val.getId().equals(value))
                    return val;
            }
            return null;
        }

    }

    public enum RingCollection{
        SOLITAIRE("63"),
        VINTAGE("64"),
        HALO("65"),
        CLASSIC("66"),
        THREE_STONE("67"),
        PAVE("68");

        String value;
        RingCollection(String value){
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        static public RingCollection valueFor(String value){
            for(RingCollection val : values()){
                if(val.getValue().equals(value))
                    return val;
            }
            return null;
        }
    }


    // TODO move following methods to processor class


    public boolean isPriceInFilterRange(String price){
        Float value = Float.valueOf(ComparatorTool.getFloatValue(price));
        return (value <= getPriceTo() && value >= getPriceFrom());
    }

    public boolean isMetalsInFilter(String metals){
        String[] allMetalsFromMap = metals.split(",");
        for (String curItem : allMetalsFromMap) {
            boolean result = false;
            if(getMetals() == null)
                return true;
            for(RingMetal metal : getMetals()){
                if(metal.getLabel().equals(curItem)) {
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

    public boolean isCollectionsInFilter(String collections){
        String[] allCollectionsFromMap = collections.split(",");
        for (String curItem : allCollectionsFromMap) {
            boolean result = false;
            if(getCollections() == null)
                return true;
            for(RingCollection collection : getCollections()){
                if(collection.name().equals(curItem)) {
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

    @Override
    public String toString() {
        return "RingFilter{" +
                "collections=" + Arrays.toString(collections) +
                ", metals=" + Arrays.toString(metals) +
                ", priceFrom=" + priceFrom +
                ", priceTo=" + priceTo +
                '}';
    }
}
//enum.valueOf("VALUE").ordinal()

