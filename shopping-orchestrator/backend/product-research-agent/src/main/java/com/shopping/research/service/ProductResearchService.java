package com.shopping.research.service;

import com.shopping.research.dto.ProductInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductResearchService {

    private static final Map<String, List<ProductInfo>> PRODUCT_DATABASE = new HashMap<>();

    static {
        // Laptops
        PRODUCT_DATABASE.put("laptop", Arrays.asList(
            ProductInfo.builder()
                .id("LAP001").name("Apple MacBook Air M2").brand("Apple").category("laptop")
                .price(99999.0).priceRange("₹90,000 - ₹1,10,000")
                .keyFeatures(Arrays.asList("Apple M2 chip", "8GB RAM", "256GB SSD", "13.6-inch Liquid Retina display", "18-hour battery", "MagSafe charging"))
                .notableHighlights("Best-in-class performance per watt, fanless design, excellent build quality")
                .rating(4.8).imageUrl("https://example.com/macbook-air-m2.jpg")
                .sourceUrl("https://www.apple.com/macbook-air-m2/").build(),

            ProductInfo.builder()
                .id("LAP002").name("Dell XPS 15").brand("Dell").category("laptop")
                .price(129999.0).priceRange("₹1,20,000 - ₹1,40,000")
                .keyFeatures(Arrays.asList("Intel Core i7-13700H", "16GB DDR5 RAM", "512GB NVMe SSD", "15.6-inch OLED display", "NVIDIA RTX 4060", "Thunderbolt 4"))
                .notableHighlights("Premium build, stunning OLED display, powerful for creative work")
                .rating(4.6).imageUrl("https://example.com/dell-xps-15.jpg")
                .sourceUrl("https://www.dell.com/xps-15").build(),

            ProductInfo.builder()
                .id("LAP003").name("Lenovo ThinkPad X1 Carbon").brand("Lenovo").category("laptop")
                .price(109999.0).priceRange("₹1,00,000 - ₹1,20,000")
                .keyFeatures(Arrays.asList("Intel Core i7-1365U", "16GB LPDDR5 RAM", "512GB SSD", "14-inch IPS display", "Military-grade durability", "Excellent keyboard"))
                .notableHighlights("Best business laptop, legendary keyboard, ultra-lightweight at 1.12kg")
                .rating(4.7).imageUrl("https://example.com/thinkpad-x1.jpg")
                .sourceUrl("https://www.lenovo.com/thinkpad-x1-carbon").build(),

            ProductInfo.builder()
                .id("LAP004").name("ASUS ROG Zephyrus G14").brand("ASUS").category("laptop")
                .price(89999.0).priceRange("₹80,000 - ₹95,000")
                .keyFeatures(Arrays.asList("AMD Ryzen 9 7940HS", "16GB DDR5 RAM", "1TB SSD", "14-inch QHD+ 165Hz display", "NVIDIA RTX 4060", "AniMe Matrix LED"))
                .notableHighlights("Best gaming laptop under 1 lakh, compact yet powerful, unique LED display")
                .rating(4.5).imageUrl("https://example.com/rog-zephyrus.jpg")
                .sourceUrl("https://rog.asus.com/zephyrus-g14").build(),

            ProductInfo.builder()
                .id("LAP005").name("HP Pavilion 15").brand("HP").category("laptop")
                .price(54999.0).priceRange("₹50,000 - ₹60,000")
                .keyFeatures(Arrays.asList("Intel Core i5-1235U", "8GB DDR4 RAM", "512GB SSD", "15.6-inch FHD IPS", "Intel Iris Xe Graphics", "Fast charge"))
                .notableHighlights("Best value for money, good for students and professionals")
                .rating(4.2).imageUrl("https://example.com/hp-pavilion.jpg")
                .sourceUrl("https://www.hp.com/pavilion-15").build()
        ));

        // Smartphones
        PRODUCT_DATABASE.put("smartphone", Arrays.asList(
            ProductInfo.builder()
                .id("PHN001").name("Samsung Galaxy S24 Ultra").brand("Samsung").category("smartphone")
                .price(134999.0).priceRange("₹1,25,000 - ₹1,45,000")
                .keyFeatures(Arrays.asList("Snapdragon 8 Gen 3", "12GB RAM", "256GB storage", "200MP quad camera", "6.8-inch Dynamic AMOLED 2X", "S Pen included", "5000mAh battery"))
                .notableHighlights("Best Android flagship, incredible camera system, built-in S Pen")
                .rating(4.8).imageUrl("https://example.com/s24-ultra.jpg")
                .sourceUrl("https://www.samsung.com/galaxy-s24-ultra").build(),

            ProductInfo.builder()
                .id("PHN002").name("Apple iPhone 15 Pro").brand("Apple").category("smartphone")
                .price(134900.0).priceRange("₹1,25,000 - ₹1,40,000")
                .keyFeatures(Arrays.asList("A17 Pro chip", "8GB RAM", "256GB storage", "48MP triple camera", "6.1-inch Super Retina XDR", "Titanium design", "USB-C"))
                .notableHighlights("Best iOS experience, titanium build, ProRes video recording")
                .rating(4.9).imageUrl("https://example.com/iphone-15-pro.jpg")
                .sourceUrl("https://www.apple.com/iphone-15-pro").build(),

            ProductInfo.builder()
                .id("PHN003").name("OnePlus 12").brand("OnePlus").category("smartphone")
                .price(64999.0).priceRange("₹60,000 - ₹70,000")
                .keyFeatures(Arrays.asList("Snapdragon 8 Gen 3", "12GB RAM", "256GB storage", "50MP Hasselblad triple camera", "6.82-inch LTPO AMOLED", "100W fast charging", "5400mAh battery"))
                .notableHighlights("Flagship killer, fastest charging, Hasselblad camera tuning")
                .rating(4.6).imageUrl("https://example.com/oneplus-12.jpg")
                .sourceUrl("https://www.oneplus.com/oneplus-12").build(),

            ProductInfo.builder()
                .id("PHN004").name("Xiaomi 14 Pro").brand("Xiaomi").category("smartphone")
                .price(74999.0).priceRange("₹70,000 - ₹80,000")
                .keyFeatures(Arrays.asList("Snapdragon 8 Gen 3", "12GB RAM", "256GB storage", "50MP Leica triple camera", "6.73-inch LTPO AMOLED", "120W HyperCharge", "50W wireless charging"))
                .notableHighlights("Leica camera partnership, fastest wireless charging, premium build")
                .rating(4.5).imageUrl("https://example.com/xiaomi-14-pro.jpg")
                .sourceUrl("https://www.mi.com/xiaomi-14-pro").build(),

            ProductInfo.builder()
                .id("PHN005").name("Realme GT 5 Pro").brand("Realme").category("smartphone")
                .price(39999.0).priceRange("₹38,000 - ₹45,000")
                .keyFeatures(Arrays.asList("Snapdragon 8 Gen 3", "12GB RAM", "256GB storage", "50MP Sony IMX890 camera", "6.78-inch LTPO AMOLED", "100W fast charging", "5400mAh battery"))
                .notableHighlights("Best value flagship, Snapdragon 8 Gen 3 at mid-range price")
                .rating(4.4).imageUrl("https://example.com/realme-gt5-pro.jpg")
                .sourceUrl("https://www.realme.com/gt-5-pro").build()
        ));

        // Headphones
        PRODUCT_DATABASE.put("headphone", Arrays.asList(
            ProductInfo.builder()
                .id("HP001").name("Sony WH-1000XM5").brand("Sony").category("headphone")
                .price(29990.0).priceRange("₹28,000 - ₹32,000")
                .keyFeatures(Arrays.asList("Industry-leading ANC", "30-hour battery", "Multipoint connection", "LDAC Hi-Res Audio", "Speak-to-Chat", "Foldable design"))
                .notableHighlights("Best noise cancellation in the market, premium sound quality")
                .rating(4.8).imageUrl("https://example.com/sony-xm5.jpg")
                .sourceUrl("https://www.sony.com/wh-1000xm5").build(),

            ProductInfo.builder()
                .id("HP002").name("Apple AirPods Max").brand("Apple").category("headphone")
                .price(59900.0).priceRange("₹55,000 - ₹62,000")
                .keyFeatures(Arrays.asList("Apple H1 chip", "Adaptive EQ", "Active Noise Cancellation", "Transparency mode", "Spatial Audio", "20-hour battery", "Premium aluminum build"))
                .notableHighlights("Best for Apple ecosystem, premium build quality, spatial audio")
                .rating(4.7).imageUrl("https://example.com/airpods-max.jpg")
                .sourceUrl("https://www.apple.com/airpods-max").build(),

            ProductInfo.builder()
                .id("HP003").name("Bose QuietComfort 45").brand("Bose").category("headphone")
                .price(24990.0).priceRange("₹22,000 - ₹27,000")
                .keyFeatures(Arrays.asList("Bose ANC", "24-hour battery", "Aware mode", "Multipoint Bluetooth", "Comfortable fit", "USB-C charging"))
                .notableHighlights("Most comfortable headphones, excellent ANC, great for long sessions")
                .rating(4.6).imageUrl("https://example.com/bose-qc45.jpg")
                .sourceUrl("https://www.bose.com/qc45").build()
        ));

        // Televisions
        PRODUCT_DATABASE.put("television", Arrays.asList(
            ProductInfo.builder()
                .id("TV001").name("Samsung 65-inch QLED 4K Q80C").brand("Samsung").category("television")
                .price(89999.0).priceRange("₹85,000 - ₹95,000")
                .keyFeatures(Arrays.asList("65-inch QLED 4K", "Quantum HDR 1500", "120Hz refresh rate", "Tizen OS", "4 HDMI ports", "Dolby Atmos", "Gaming Hub"))
                .notableHighlights("Excellent picture quality, great for gaming, smart TV features")
                .rating(4.6).imageUrl("https://example.com/samsung-q80c.jpg")
                .sourceUrl("https://www.samsung.com/q80c").build(),

            ProductInfo.builder()
                .id("TV002").name("LG C3 OLED 55-inch").brand("LG").category("television")
                .price(129999.0).priceRange("₹1,20,000 - ₹1,40,000")
                .keyFeatures(Arrays.asList("55-inch OLED 4K", "α9 AI Processor Gen6", "120Hz refresh rate", "webOS 23", "Dolby Vision IQ", "4 HDMI 2.1 ports", "G-Sync & FreeSync"))
                .notableHighlights("Best OLED TV, perfect blacks, best for gaming and movies")
                .rating(4.9).imageUrl("https://example.com/lg-c3.jpg")
                .sourceUrl("https://www.lg.com/c3-oled").build(),

            ProductInfo.builder()
                .id("TV003").name("Sony Bravia XR 55-inch").brand("Sony").category("television")
                .price(109999.0).priceRange("₹1,00,000 - ₹1,20,000")
                .keyFeatures(Arrays.asList("55-inch 4K OLED", "Cognitive Processor XR", "120Hz", "Google TV", "Acoustic Surface Audio+", "HDMI 2.1"))
                .notableHighlights("Best sound quality in a TV, Google TV integration, XR processor")
                .rating(4.7).imageUrl("https://example.com/sony-bravia-xr.jpg")
                .sourceUrl("https://www.sony.com/bravia-xr").build()
        ));

        // Cameras
        PRODUCT_DATABASE.put("camera", Arrays.asList(
            ProductInfo.builder()
                .id("CAM001").name("Sony Alpha A7 IV").brand("Sony").category("camera")
                .price(229990.0).priceRange("₹2,20,000 - ₹2,40,000")
                .keyFeatures(Arrays.asList("33MP full-frame BSI CMOS", "4K 60fps video", "759 phase-detect AF points", "10fps burst", "5-axis IBIS", "Dual card slots"))
                .notableHighlights("Best all-around mirrorless camera, excellent for both photo and video")
                .rating(4.8).imageUrl("https://example.com/sony-a7iv.jpg")
                .sourceUrl("https://www.sony.com/alpha-a7iv").build(),

            ProductInfo.builder()
                .id("CAM002").name("Canon EOS R6 Mark II").brand("Canon").category("camera")
                .price(219990.0).priceRange("₹2,10,000 - ₹2,30,000")
                .keyFeatures(Arrays.asList("24.2MP full-frame CMOS", "4K 60fps RAW video", "40fps burst", "Dual Pixel CMOS AF II", "8-stop IBIS", "Weather sealed"))
                .notableHighlights("Best for sports and wildlife, incredible AF tracking, fast burst rate")
                .rating(4.7).imageUrl("https://example.com/canon-r6-ii.jpg")
                .sourceUrl("https://www.canon.com/eos-r6-mark-ii").build()
        ));

        // Tablets
        PRODUCT_DATABASE.put("tablet", Arrays.asList(
            ProductInfo.builder()
                .id("TAB001").name("Apple iPad Pro 12.9-inch M2").brand("Apple").category("tablet")
                .price(112900.0).priceRange("₹1,05,000 - ₹1,20,000")
                .keyFeatures(Arrays.asList("Apple M2 chip", "12.9-inch Liquid Retina XDR", "8GB RAM", "256GB storage", "Thunderbolt 4", "Apple Pencil 2 support", "5G capable"))
                .notableHighlights("Most powerful tablet, ProMotion display, desktop-class performance")
                .rating(4.8).imageUrl("https://example.com/ipad-pro-m2.jpg")
                .sourceUrl("https://www.apple.com/ipad-pro").build(),

            ProductInfo.builder()
                .id("TAB002").name("Samsung Galaxy Tab S9+").brand("Samsung").category("tablet")
                .price(89999.0).priceRange("₹85,000 - ₹95,000")
                .keyFeatures(Arrays.asList("Snapdragon 8 Gen 2", "12GB RAM", "256GB storage", "12.4-inch Dynamic AMOLED 2X", "S Pen included", "IP68 water resistant", "DeX mode"))
                .notableHighlights("Best Android tablet, included S Pen, DeX desktop mode")
                .rating(4.7).imageUrl("https://example.com/galaxy-tab-s9-plus.jpg")
                .sourceUrl("https://www.samsung.com/galaxy-tab-s9-plus").build()
        ));

        // Refrigerators
        PRODUCT_DATABASE.put("refrigerator", Arrays.asList(
            ProductInfo.builder()
                .id("REF001").name("Samsung 653L French Door Refrigerator").brand("Samsung").category("refrigerator")
                .price(89990.0).priceRange("₹85,000 - ₹95,000")
                .keyFeatures(Arrays.asList("653L capacity", "French door design", "Twin Cooling Plus", "Digital Inverter Compressor", "SpaceMax technology", "Wi-Fi connectivity"))
                .notableHighlights("Best large family refrigerator, energy efficient, smart features")
                .rating(4.5).imageUrl("https://example.com/samsung-french-door.jpg")
                .sourceUrl("https://www.samsung.com/refrigerator").build(),

            ProductInfo.builder()
                .id("REF002").name("LG 655L Side-by-Side Refrigerator").brand("LG").category("refrigerator")
                .price(79990.0).priceRange("₹75,000 - ₹85,000")
                .keyFeatures(Arrays.asList("655L capacity", "Side-by-side design", "InstaView Door-in-Door", "Linear Cooling", "Smart Diagnosis", "Multi Air Flow"))
                .notableHighlights("InstaView window, excellent cooling technology, smart features")
                .rating(4.4).imageUrl("https://example.com/lg-side-by-side.jpg")
                .sourceUrl("https://www.lg.com/refrigerator").build()
        ));

        // Washing Machines
        PRODUCT_DATABASE.put("washing machine", Arrays.asList(
            ProductInfo.builder()
                .id("WM001").name("LG 9kg Front Load Washing Machine").brand("LG").category("washing machine")
                .price(54990.0).priceRange("₹50,000 - ₹60,000")
                .keyFeatures(Arrays.asList("9kg capacity", "AI DD technology", "Steam wash", "TurboWash 360", "Wi-Fi connectivity", "1400 RPM", "6 Motion Direct Drive"))
                .notableHighlights("Best front load washer, AI fabric detection, steam cleaning")
                .rating(4.6).imageUrl("https://example.com/lg-front-load.jpg")
                .sourceUrl("https://www.lg.com/washing-machine").build(),

            ProductInfo.builder()
                .id("WM002").name("Samsung 8kg Front Load Washing Machine").brand("Samsung").category("washing machine")
                .price(44990.0).priceRange("₹40,000 - ₹50,000")
                .keyFeatures(Arrays.asList("8kg capacity", "EcoBubble technology", "Digital Inverter Motor", "QuickDrive", "Wi-Fi SmartThings", "1400 RPM"))
                .notableHighlights("EcoBubble for gentle yet effective cleaning, energy efficient")
                .rating(4.5).imageUrl("https://example.com/samsung-front-load.jpg")
                .sourceUrl("https://www.samsung.com/washing-machine").build()
        ));
    }

    public List<ProductInfo> researchProducts(String category, Double budget, String preferences, String brand) {
        log.info("Researching products for category: {}, budget: {}, preferences: {}", category, budget, preferences);

        String normalizedCategory = category.toLowerCase().trim();

        // Find matching category
        List<ProductInfo> products = PRODUCT_DATABASE.entrySet().stream()
                .filter(entry -> normalizedCategory.contains(entry.getKey()) || entry.getKey().contains(normalizedCategory))
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toList());

        // If no exact match, return general products
        if (products.isEmpty()) {
            log.warn("No products found for category: {}. Returning sample products.", category);
            products = generateGenericProducts(category, budget);
        }

        // Filter by brand if specified
        if (brand != null && !brand.isEmpty()) {
            List<ProductInfo> brandFiltered = products.stream()
                    .filter(p -> p.getBrand().equalsIgnoreCase(brand))
                    .collect(Collectors.toList());
            if (!brandFiltered.isEmpty()) {
                products = brandFiltered;
            }
        }

        // Sort by relevance (rating desc, then price asc)
        products.sort((a, b) -> {
            int ratingCompare = Double.compare(b.getRating(), a.getRating());
            if (ratingCompare != 0) return ratingCompare;
            return Double.compare(a.getPrice(), b.getPrice());
        });

        // Return top 5 products
        return products.stream().limit(5).collect(Collectors.toList());
    }

    private List<ProductInfo> generateGenericProducts(String category, Double budget) {
        List<ProductInfo> generic = new ArrayList<>();
        String[] brands = {"Samsung", "LG", "Sony", "Philips", "Bosch"};
        for (int i = 1; i <= 3; i++) {
            double price = budget != null ? budget * (0.7 + (i * 0.1)) : 10000.0 * i;
            generic.add(ProductInfo.builder()
                    .id("GEN00" + i)
                    .name(brands[i-1] + " " + category + " Model " + (2024 + i))
                    .brand(brands[i-1])
                    .category(category)
                    .price(price)
                    .priceRange("₹" + (long)(price * 0.9) + " - ₹" + (long)(price * 1.1))
                    .keyFeatures(Arrays.asList("Feature 1", "Feature 2", "Feature 3", "Energy Efficient", "1 Year Warranty"))
                    .notableHighlights("Good value for money product in " + category + " category")
                    .rating(4.0 + (i * 0.1))
                    .imageUrl("https://example.com/" + category.replace(" ", "-") + "-" + i + ".jpg")
                    .sourceUrl("https://example.com/" + category.replace(" ", "-"))
                    .build());
        }
        return generic;
    }
}
