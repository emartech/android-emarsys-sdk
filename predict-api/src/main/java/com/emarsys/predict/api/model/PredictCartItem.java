package com.emarsys.predict.api.model;

public class PredictCartItem implements CartItem {
    private String id;
    private double price;
    private double quantity;

    public PredictCartItem(String id, double price, double quantity) {
        this.id = id;
        this.price = price;
        this.quantity = quantity;
    }

    @Override
    public String getItemId() {
        return id;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public double getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PredictCartItem that = (PredictCartItem) o;

        if (Double.compare(that.price, price) != 0) return false;
        if (Double.compare(that.quantity, quantity) != 0) return false;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id != null ? id.hashCode() : 0;
        temp = Double.doubleToLongBits(price);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(quantity);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "PredictCartItem{" +
                "id='" + id + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                '}';
    }
}
