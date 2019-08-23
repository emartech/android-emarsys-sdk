package com.emarsys.predict.api.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.emarsys.core.util.Assert;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Product {
    @NonNull
    private String productId;

    @NonNull
    private String title;

    @NonNull
    private URL linkUrl;

    @NonNull
    private String feature;

    @NonNull
    private Map<String, String> customFields;

    @Nullable
    private URL imageUrl;

    @Nullable
    private URL zoomImageUrl;

    @Nullable
    private String categoryPath;

    @Nullable
    private Boolean available;

    @Nullable
    private String productDescription;

    @Nullable
    private Float price;

    @Nullable
    private Float msrp;

    @Nullable
    private String album;

    @Nullable
    private String actor;

    @Nullable
    private String artist;

    @Nullable
    private String author;

    @Nullable
    private String brand;

    @Nullable
    private Integer year;

    Product(@NonNull String productId, @NonNull String title, @NonNull String linkUrl, @NonNull String feature, @NonNull Map<String, String> customFields, @Nullable String imageUrl, @Nullable String zoomImageUrl, @Nullable String categoryPath, @Nullable Boolean available, @Nullable String productDescription, @Nullable Float price, @Nullable Float msrp, @Nullable String album, @Nullable String actor, @Nullable String artist, @Nullable String author, @Nullable String brand, @Nullable Integer year) {
        Assert.notNull(productId, "ProductId must not be null!");
        Assert.notNull(title, "Title must not be null!");
        Assert.notNull(linkUrl, "LinkUrl must not be null!");
        Assert.notNull(customFields, "CustomFields must not be null!");
        Assert.notNull(feature, "Feature must not be null!");

        this.productId = productId;
        this.title = title;
        try {
            this.linkUrl = new URL(linkUrl);
            if (imageUrl != null) {
                this.imageUrl = new URL(imageUrl);
            }
            if (zoomImageUrl != null) {
                this.zoomImageUrl = new URL(zoomImageUrl);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.feature = feature;
        this.customFields = customFields;
        this.categoryPath = categoryPath;
        this.available = available;
        this.productDescription = productDescription;
        this.price = price;
        this.msrp = msrp;
        this.album = album;
        this.actor = actor;
        this.artist = artist;
        this.author = author;
        this.brand = brand;
        this.year = year;
    }

    @NonNull
    public String getProductId() {
        return productId;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public URL getLinkUrl() {
        return linkUrl;
    }

    @NonNull
    public String getFeature() {
        return feature;
    }

    @NonNull
    public Map<String, String> getCustomFields() {
        return customFields;
    }

    @Nullable
    public URL getImageUrl() {
        return imageUrl;
    }

    @Nullable
    public URL getZoomImageUrl() {
        return zoomImageUrl;
    }

    @Nullable
    public String getCategoryPath() {
        return categoryPath;
    }

    @Nullable
    public Boolean getAvailable() {
        return available;
    }

    @Nullable
    public String getProductDescription() {
        return productDescription;
    }

    @Nullable
    public Float getPrice() {
        return price;
    }

    @Nullable
    public Float getMsrp() {
        return msrp;
    }

    @Nullable
    public String getAlbum() {
        return album;
    }

    @Nullable
    public String getActor() {
        return actor;
    }

    @Nullable
    public String getArtist() {
        return artist;
    }

    @Nullable
    public String getAuthor() {
        return author;
    }

    @Nullable
    public String getBrand() {
        return brand;
    }

    @Nullable
    public Integer getYear() {
        return year;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return productId.equals(product.productId) &&
                title.equals(product.title) &&
                linkUrl.equals(product.linkUrl) &&
                feature.equals(product.feature) &&
                customFields.equals(product.customFields) &&
                Objects.equals(imageUrl, product.imageUrl) &&
                Objects.equals(zoomImageUrl, product.zoomImageUrl) &&
                Objects.equals(categoryPath, product.categoryPath) &&
                Objects.equals(available, product.available) &&
                Objects.equals(productDescription, product.productDescription) &&
                Objects.equals(price, product.price) &&
                Objects.equals(msrp, product.msrp) &&
                Objects.equals(album, product.album) &&
                Objects.equals(actor, product.actor) &&
                Objects.equals(artist, product.artist) &&
                Objects.equals(author, product.author) &&
                Objects.equals(brand, product.brand) &&
                Objects.equals(year, product.year);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, title, linkUrl, feature, customFields, imageUrl, zoomImageUrl, categoryPath, available, productDescription, price, msrp, album, actor, artist, author, brand, year);
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ", title='" + title + '\'' +
                ", linkUrl=" + linkUrl +
                ", feature='" + feature + '\'' +
                ", customFields=" + customFields +
                ", imageUrl=" + imageUrl +
                ", zoomImageUrl=" + zoomImageUrl +
                ", categoryPath='" + categoryPath + '\'' +
                ", available=" + available +
                ", productDescription='" + productDescription + '\'' +
                ", price=" + price +
                ", msrp=" + msrp +
                ", album='" + album + '\'' +
                ", actor='" + actor + '\'' +
                ", artist='" + artist + '\'' +
                ", author='" + author + '\'' +
                ", brand='" + brand + '\'' +
                ", year=" + year +
                '}';
    }

    public static class Builder {
        private String productId;
        private String title;
        private String linkUrl;
        private String feature;
        private Map<String, String> customFields;
        private String imageUrl;
        private String zoomImageUrl;
        private String categoryPath;
        private Boolean available;
        private String productDescription;
        private Float price;
        private Float msrp;
        private String album;
        private String actor;
        private String artist;
        private String author;
        private String brand;
        private Integer year;

        public Builder(String productId, String title, String linkUrl, String feature) {
            this.productId = productId;
            this.title = title;
            this.linkUrl = linkUrl;
            this.feature = feature;
        }

        public Builder customFields(Map<String, String> customFields) {
            this.customFields = customFields;
            return this;
        }

        public Builder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder zoomImageUrl(String zoomImageUrl) {
            this.zoomImageUrl = zoomImageUrl;
            return this;
        }

        public Builder categoryPath(String categoryPath) {
            this.categoryPath = categoryPath;
            return this;
        }

        public Builder available(Boolean available) {
            this.available = available;
            return this;
        }

        public Builder productDescription(String productDescription) {
            this.productDescription = productDescription;
            return this;
        }

        public Builder price(Float price) {
            this.price = price;
            return this;
        }

        public Builder msrp(Float msrp) {
            this.msrp = msrp;
            return this;
        }

        public Builder album(String album) {
            this.album = album;
            return this;
        }

        public Builder actor(String actor) {
            this.actor = actor;
            return this;
        }

        public Builder artist(String artist) {
            this.artist = artist;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public Builder brand(String brand) {
            this.brand = brand;
            return this;
        }

        public Builder year(Integer year) {
            this.year = year;
            return this;
        }

        public Product build() {
            if (customFields == null) {
                customFields = new HashMap<>();
            }
            return new Product(productId, title, linkUrl, feature, customFields, imageUrl, zoomImageUrl, categoryPath, available, productDescription, price, msrp, album, actor, artist, author, brand, year);
        }
    }
}
