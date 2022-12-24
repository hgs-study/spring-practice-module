package practice.pir.domain;

public class UnlistedStock {
    public enum UnlistedStockState{ UNLISTED, LISTED, DELISTED};

    private String code;
    private String name;
    private UnlistedStockState unlistedStockState;
    private Boolean activation;

    public UnlistedStock(String code, String name, Boolean activation) {
        this.code = code;
        this.name = name;
        this.activation = activation;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public UnlistedStockState getUnlistedStockState() {
        return unlistedStockState;
    }

    public Boolean getActivation() {
        return activation;
    }

    public void unlisted(){
        unlistedStockState = UnlistedStockState.UNLISTED;
    }

    public void listed(){
        unlistedStockState = UnlistedStockState.LISTED;
    }

    public void Inactivation(){
        activation = false;
    }
}
