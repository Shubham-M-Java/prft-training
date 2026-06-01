package com.shopping.research.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopping.research.dto.ProductInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductResearchService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // RapidAPI config
    @Value("${rapidapi.key:}")
    private String rapidApiKey;

    @Value("${rapidapi.amazon.host:real-time-amazon-data.p.rapidapi.com}")
    private String amazonHost;

    @Value("${rapidapi.amazon.enabled:false}")
    private boolean amazonEnabled;

    @Value("${rapidapi.flipkart.host:real-time-flipkart-api.p.rapidapi.com}")
    private String flipkartHost;

    @Value("${rapidapi.flipkart.enabled:false}")
    private boolean flipkartEnabled;

    public ProductResearchService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC ENTRY POINT
    // ─────────────────────────────────────────────────────────────────────────

    public List<ProductInfo> researchProducts(String category, Double budget,
                                               String preferences, String brand) {
        log.info("Researching products – category: {}, budget: {}, preferences: {}, brand: {}",
                category, budget, preferences, brand);

        List<ProductInfo> products = new ArrayList<>();

        // 1. Try Amazon RapidAPI
        if (amazonEnabled && rapidApiKey != null && !rapidApiKey.isBlank()) {
            try {
                List<ProductInfo> amazonProducts = fetchFromAmazon(category, budget, brand);
                products.addAll(amazonProducts);
                log.info("Fetched {} products from Amazon", amazonProducts.size());
            } catch (Exception e) {
                log.warn("Amazon API call failed: {}", e.getMessage());
            }
        }

        // 2. Try Flipkart RapidAPI
        if (flipkartEnabled && rapidApiKey != null && !rapidApiKey.isBlank()) {
            try {
                List<ProductInfo> flipkartProducts = fetchFromFlipkart(category, budget, brand);
                products.addAll(flipkartProducts);
                log.info("Fetched {} products from Flipkart", flipkartProducts.size());
            } catch (Exception e) {
                log.warn("Flipkart API call failed: {}", e.getMessage());
            }
        }

        // 3. Fallback to mock data if no real data
        if (products.isEmpty()) {
            log.info("No real API data available – using mock data for category: {}", category);
            products = getMockProducts(category, budget, brand);
        }

        // Filter by brand if specified and not already filtered
        if (brand != null && !brand.isBlank()) {
            List<ProductInfo> brandFiltered = products.stream()
                    .filter(p -> p.getBrand() != null &&
                                 p.getBrand().equalsIgnoreCase(brand))
                    .collect(Collectors.toList());
            if (!brandFiltered.isEmpty()) {
                products = brandFiltered;
            }
        }

        // Sort: rating desc, then price asc
        products.sort((a, b) -> {
            double ra = a.getRating() != null ? a.getRating() : 0;
            double rb = b.getRating() != null ? b.getRating() : 0;
            int cmp = Double.compare(rb, ra);
            if (cmp != 0) return cmp;
            double pa = a.getPrice() != null ? a.getPrice() : 0;
            double pb = b.getPrice() != null ? b.getPrice() : 0;
            return Double.compare(pa, pb);
        });

        return products.stream().limit(5).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AMAZON  (RapidAPI: real-time-amazon-data.p.rapidapi.com)
    // ─────────────────────────────────────────────────────────────────────────

    private List<ProductInfo> fetchFromAmazon(String category, Double budget, String brand) throws Exception {
        String query = buildQuery(category, brand);
        String url = "https://" + amazonHost + "/search?query=" +
                     encodeParam(query) + "&page=1&country=IN&sort_by=RELEVANCE&product_condition=ALL";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RapidAPI-Key", rapidApiKey);
        headers.set("X-RapidAPI-Host", amazonHost);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            return Collections.emptyList();
        }

        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode items = root.path("data").path("products");
        if (items.isMissingNode() || !items.isArray()) {
            return Collections.emptyList();
        }

        List<ProductInfo> result = new ArrayList<>();
        for (JsonNode item : items) {
            try {
                Double price = parsePrice(item.path("product_price").asText("0"));
                if (budget != null && price != null && price > budget * 1.2) continue;

                String priceStr = item.path("product_price").asText("");
                String originalPrice = item.path("product_original_price").asText("");
                String discount = "";
                if (!originalPrice.isBlank() && !priceStr.isBlank()) {
                    discount = item.path("product_star_rating").asText("") + "% off";
                }

                List<String> features = new ArrayList<>();
                JsonNode specs = item.path("product_specifications");
                if (specs.isArray()) {
                    for (JsonNode spec : specs) {
                        String key = spec.path("name").asText("");
                        String val = spec.path("value").asText("");
                        if (!key.isBlank() && !val.isBlank()) {
                            features.add(key + ": " + val);
                        }
                    }
                }
                if (features.isEmpty()) {
                    String desc = item.path("product_description").asText("");
                    if (!desc.isBlank()) {
                        features = Arrays.asList(desc.split("[.;]")).stream()
                                .map(String::trim).filter(s -> !s.isBlank())
                                .limit(5).collect(Collectors.toList());
                    }
                }

                String ratingStr = item.path("product_star_rating").asText("0")
                        .replaceAll("[^0-9.]", "");
                double rating = ratingStr.isBlank() ? 0 : Double.parseDouble(ratingStr);

                String reviewStr = item.path("product_num_ratings").asText("0")
                        .replaceAll("[^0-9]", "");
                int reviews = reviewStr.isBlank() ? 0 : Integer.parseInt(reviewStr);

                result.add(ProductInfo.builder()
                        .id("AMZ-" + item.path("asin").asText(UUID.randomUUID().toString().substring(0, 8)))
                        .name(item.path("product_title").asText("Unknown Product"))
                        .brand(extractBrand(item.path("product_title").asText("")))
                        .category(category)
                        .price(price)
                        .priceRange(priceStr.isBlank() ? "Price not available" : priceStr)
                        .keyFeatures(features.isEmpty()
                                ? Arrays.asList("See product page for details") : features)
                        .notableHighlights(item.path("product_description").asText(
                                "Top-rated product on Amazon India"))
                        .rating(rating)
                        .reviewCount(reviews)
                        .imageUrl(item.path("product_photo").asText(""))
                        .sourceUrl(item.path("product_url").asText("https://www.amazon.in"))
                        .platform("Amazon")
                        .availability(item.path("is_prime").asBoolean(false)
                                ? "In Stock (Prime)" : "In Stock")
                        .discount(discount)
                        .build());
            } catch (Exception e) {
                log.debug("Skipping Amazon item due to parse error: {}", e.getMessage());
            }
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FLIPKART  (RapidAPI: real-time-flipkart-api.p.rapidapi.com)
    // ─────────────────────────────────────────────────────────────────────────

    private List<ProductInfo> fetchFromFlipkart(String category, Double budget, String brand) throws Exception {
        String query = buildQuery(category, brand);
        String url = "https://" + flipkartHost + "/product/search?q=" +
                     encodeParam(query) + "&page=1";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RapidAPI-Key", rapidApiKey);
        headers.set("X-RapidAPI-Host", flipkartHost);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            return Collections.emptyList();
        }

        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode items = root.path("products");
        if (items.isMissingNode() || !items.isArray()) {
            // Try alternate path
            items = root.path("data").path("products");
        }
        if (items.isMissingNode() || !items.isArray()) {
            return Collections.emptyList();
        }

        List<ProductInfo> result = new ArrayList<>();
        for (JsonNode item : items) {
            try {
                Double price = parsePrice(item.path("price").asText(
                        item.path("selling_price").asText("0")));
                if (budget != null && price != null && price > budget * 1.2) continue;

                String priceStr = "₹" + (price != null ? String.format("%,.0f", price) : "N/A");
                String mrp = item.path("mrp").asText("");
                String discount = item.path("discount").asText("");

                List<String> features = new ArrayList<>();
                JsonNode highlights = item.path("highlights");
                if (highlights.isArray()) {
                    for (JsonNode h : highlights) {
                        features.add(h.asText());
                    }
                }
                JsonNode specs = item.path("specifications");
                if (features.isEmpty() && specs.isArray()) {
                    for (JsonNode spec : specs) {
                        String key = spec.path("key").asText("");
                        String val = spec.path("value").asText("");
                        if (!key.isBlank()) features.add(key + ": " + val);
                    }
                }

                String ratingStr = item.path("rating").asText(
                        item.path("average_rating").asText("0"))
                        .replaceAll("[^0-9.]", "");
                double rating = ratingStr.isBlank() ? 0 : Double.parseDouble(ratingStr);

                String reviewStr = item.path("rating_count").asText(
                        item.path("num_reviews").asText("0"))
                        .replaceAll("[^0-9]", "");
                int reviews = reviewStr.isBlank() ? 0 : Integer.parseInt(reviewStr);

                String productId = item.path("pid").asText(
                        item.path("id").asText(UUID.randomUUID().toString().substring(0, 8)));

                result.add(ProductInfo.builder()
                        .id("FLK-" + productId)
                        .name(item.path("name").asText(item.path("title").asText("Unknown Product")))
                        .brand(item.path("brand").asText(
                                extractBrand(item.path("name").asText(""))))
                        .category(category)
                        .price(price)
                        .priceRange(priceStr + (mrp.isBlank() ? "" : " (MRP: ₹" + mrp + ")"))
                        .keyFeatures(features.isEmpty()
                                ? Arrays.asList("See product page for details") : features)
                        .notableHighlights(item.path("description").asText(
                                "Available on Flipkart"))
                        .rating(rating)
                        .reviewCount(reviews)
                        .imageUrl(item.path("image").asText(
                                item.path("thumbnail").asText("")))
                        .sourceUrl(item.path("url").asText(
                                item.path("product_url").asText("https://www.flipkart.com")))
                        .platform("Flipkart")
                        .availability(item.path("in_stock").asBoolean(true)
                                ? "In Stock" : "Out of Stock")
                        .discount(discount)
                        .build());
            } catch (Exception e) {
                log.debug("Skipping Flipkart item due to parse error: {}", e.getMessage());
            }
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MOCK DATA (fallback)
    // ─────────────────────────────────────────────────────────────────────────

    private static final Map<String, List<ProductInfo>> MOCK_DB = new HashMap<>();

    static {
        // Laptops
        MOCK_DB.put("laptop", Arrays.asList(
            ProductInfo.builder()
                .id("LAP001").name("Apple MacBook Air M2").brand("Apple").category("laptop")
                .price(99999.0).priceRange("₹90,000 - ₹1,10,000")
                .keyFeatures(Arrays.asList("Apple M2 chip", "8GB RAM", "256GB SSD",
                        "13.6-inch Liquid Retina display", "18-hour battery", "MagSafe charging"))
                .notableHighlights("Best-in-class performance per watt, fanless design, excellent build quality")
                .rating(4.8).reviewCount(12450).imageUrl("https://m.media-amazon.com/images/I/71f5Eu5lJSL._SL1500_.jpg")
                .sourceUrl("https://www.amazon.in/s?k=MacBook+Air+M2").platform("Mock")
                .availability("In Stock").discount("").build(),

            ProductInfo.builder()
                .id("LAP002").name("Dell XPS 15").brand("Dell").category("laptop")
                .price(129999.0).priceRange("₹1,20,000 - ₹1,40,000")
                .keyFeatures(Arrays.asList("Intel Core i7-13700H", "16GB DDR5 RAM", "512GB NVMe SSD",
                        "15.6-inch OLED display", "NVIDIA RTX 4060", "Thunderbolt 4"))
                .notableHighlights("Premium build, stunning OLED display, powerful for creative work")
                .rating(4.6).reviewCount(8320).imageUrl("https://m.media-amazon.com/images/I/81+7Up7IWuL._SL1500_.jpg")
                .sourceUrl("https://www.amazon.in/s?k=Dell+XPS+15").platform("Mock")
                .availability("In Stock").discount("5% off").build(),

            ProductInfo.builder()
                .id("LAP003").name("Lenovo ThinkPad X1 Carbon").brand("Lenovo").category("laptop")
                .price(109999.0).priceRange("₹1,00,000 - ₹1,20,000")
                .keyFeatures(Arrays.asList("Intel Core i7-1365U", "16GB LPDDR5 RAM", "512GB SSD",
                        "14-inch IPS display", "Military-grade durability", "Excellent keyboard"))
                .notableHighlights("Best business laptop, legendary keyboard, ultra-lightweight at 1.12kg")
                .rating(4.7).reviewCount(6780).imageUrl("https://m.media-amazon.com/images/I/71Vy4KCQKZL._SL1500_.jpg")
                .sourceUrl("https://www.flipkart.com/search?q=ThinkPad+X1+Carbon").platform("Mock")
                .availability("In Stock").discount("8% off").build(),

            ProductInfo.builder()
                .id("LAP004").name("ASUS ROG Zephyrus G14").brand("ASUS").category("laptop")
                .price(89999.0).priceRange("₹80,000 - ₹95,000")
                .keyFeatures(Arrays.asList("AMD Ryzen 9 7940HS", "16GB DDR5 RAM", "1TB SSD",
                        "14-inch QHD+ 165Hz display", "NVIDIA RTX 4060", "AniMe Matrix LED"))
                .notableHighlights("Best gaming laptop under 1 lakh, compact yet powerful")
                .rating(4.5).reviewCount(9100).imageUrl("https://m.media-amazon.com/images/I/81Ot9QXJKWL._SL1500_.jpg")
                .sourceUrl("https://www.amazon.in/s?k=ASUS+ROG+Zephyrus+G14").platform("Mock")
                .availability("In Stock").discount("10% off").build(),

            ProductInfo.builder()
                .id("LAP005").name("HP Pavilion 15").brand("HP").category("laptop")
                .price(54999.0).priceRange("₹50,000 - ₹60,000")
                .keyFeatures(Arrays.asList("Intel Core i5-1235U", "8GB DDR4 RAM", "512GB SSD",
                        "15.6-inch FHD IPS", "Intel Iris Xe Graphics", "Fast charge"))
                .notableHighlights("Best value for money, good for students and professionals")
                .rating(4.2).reviewCount(15600).imageUrl("https://m.media-amazon.com/images/I/71xb2xkN5qL._SL1500_.jpg")
                .sourceUrl("https://www.flipkart.com/search?q=HP+Pavilion+15").platform("Mock")
                .availability("In Stock").discount("12% off").build()
        ));

        // Smartphones
        MOCK_DB.put("smartphone", Arrays.asList(
            ProductInfo.builder()
                .id("PHN001").name("Samsung Galaxy S24 Ultra").brand("Samsung").category("smartphone")
                .price(134999.0).priceRange("₹1,25,000 - ₹1,45,000")
                .keyFeatures(Arrays.asList("Snapdragon 8 Gen 3", "12GB RAM", "256GB storage",
                        "200MP quad camera", "6.8-inch Dynamic AMOLED 2X", "S Pen included", "5000mAh battery"))
                .notableHighlights("Best Android flagship, incredible camera system, built-in S Pen")
                .rating(4.8).reviewCount(22300).imageUrl("https://m.media-amazon.com/images/I/71Sa3dqTqzL._SL1500_.jpg")
                .sourceUrl("https://www.amazon.in/s?k=Samsung+Galaxy+S24+Ultra").platform("Mock")
                .availability("In Stock").discount("5% off").build(),

            ProductInfo.builder()
                .id("PHN002").name("Apple iPhone 15 Pro").brand("Apple").category("smartphone")
                .price(134900.0).priceRange("₹1,25,000 - ₹1,40,000")
                .keyFeatures(Arrays.asList("A17 Pro chip", "8GB RAM", "256GB storage",
                        "48MP triple camera", "6.1-inch Super Retina XDR", "Titanium design", "USB-C"))
                .notableHighlights("Best iOS experience, titanium build, ProRes video recording")
                .rating(4.9).reviewCount(31200).imageUrl("https://m.media-amazon.com/images/I/61bK6PMOC3L._SL1500_.jpg")
                .sourceUrl("https://www.amazon.in/s?k=iPhone+15+Pro").platform("Mock")
                .availability("In Stock").discount("").build(),

            ProductInfo.builder()
                .id("PHN003").name("OnePlus 12").brand("OnePlus").category("smartphone")
                .price(64999.0).priceRange("₹60,000 - ₹70,000")
                .keyFeatures(Arrays.asList("Snapdragon 8 Gen 3", "12GB RAM", "256GB storage",
                        "50MP Hasselblad triple camera", "6.82-inch LTPO AMOLED",
                        "100W fast charging", "5400mAh battery"))
                .notableHighlights("Flagship killer, fastest charging, Hasselblad camera tuning")
                .rating(4.6).reviewCount(18700).imageUrl("https://m.media-amazon.com/images/I/61U3jn0OTYL._SL1500_.jpg")
                .sourceUrl("https://www.amazon.in/s?k=OnePlus+12").platform("Mock")
                .availability("In Stock").discount("7% off").build(),

            ProductInfo.builder()
                .id("PHN004").name("Xiaomi 14 Pro").brand("Xiaomi").category("smartphone")
                .price(74999.0).priceRange("₹70,000 - ₹80,000")
                .keyFeatures(Arrays.asList("Snapdragon 8 Gen 3", "12GB RAM", "256GB storage",
                        "50MP Leica triple camera", "6.73-inch LTPO AMOLED",
                        "120W HyperCharge", "50W wireless charging"))
                .notableHighlights("Leica camera partnership, fastest wireless charging, premium build")
                .rating(4.5).reviewCount(9800).imageUrl("https://m.media-amazon.com/images/I/71Swqqe7XAL._SL1500_.jpg")
                .sourceUrl("https://www.flipkart.com/search?q=Xiaomi+14+Pro").platform("Mock")
                .availability("In Stock").discount("10% off").build(),

            ProductInfo.builder()
                .id("PHN005").name("Realme GT 5 Pro").brand("Realme").category("smartphone")
                .price(39999.0).priceRange("₹38,000 - ₹45,000")
                .keyFeatures(Arrays.asList("Snapdragon 8 Gen 3", "12GB RAM", "256GB storage",
                        "50MP Sony IMX890 camera", "6.78-inch LTPO AMOLED",
                        "100W fast charging", "5400mAh battery"))
                .notableHighlights("Best value flagship, Snapdragon 8 Gen 3 at mid-range price")
                .rating(4.4).reviewCount(7600).imageUrl("https://m.media-amazon.com/images/I/71Vy4KCQKZL._SL1500_.jpg")
                .sourceUrl("https://www.flipkart.com/search?q=Realme+GT+5+Pro").platform("Mock")
                .availability("In Stock").discount("15% off").build()
        ));

        // Headphones
        MOCK_DB.put("headphone", Arrays.asList(
            ProductInfo.builder()
                .id("HP001").name("Sony WH-1000XM5").brand("Sony").category("headphone")
                .price(29990.0).priceRange("₹28,000 - ₹32,000")
                .keyFeatures(Arrays.asList("Industry-leading ANC", "30-hour battery",
                        "Multipoint connection", "LDAC Hi-Res Audio", "Speak-to-Chat", "Foldable design"))
                .notableHighlights("Best noise cancellation in the market, premium sound quality")
                .rating(4.8).reviewCount(19400).imageUrl("https://m.media-amazon.com/images/I/71o8Q5XJS5L._SL1500_.jpg")
                .sourceUrl("https://www.amazon.in/s?k=Sony+WH-1000XM5").platform("Mock")
                .availability("In Stock").discount("20% off").build(),

            ProductInfo.builder()
                .id("HP002").name("Apple AirPods Max").brand("Apple").category("headphone")
                .price(59900.0).priceRange("₹55,000 - ₹62,000")
                .keyFeatures(Arrays.asList("Apple H1 chip", "Adaptive EQ", "Active Noise Cancellation",
                        "Transparency mode", "Spatial Audio", "20-hour battery", "Premium aluminum build"))
                .notableHighlights("Best for Apple ecosystem, premium build quality, spatial audio")
                .rating(4.7).reviewCount(8900).imageUrl("https://m.media-amazon.com/images/I/81Ot9QXJKWL._SL1500_.jpg")
                .sourceUrl("https://www.amazon.in/s?k=AirPods+Max").platform("Mock")
                .availability("In Stock").discount("").build(),

            ProductInfo.builder()
                .id("HP003").name("Bose QuietComfort 45").brand("Bose").category("headphone")
                .price(24990.0).priceRange("₹22,000 - ₹27,000")
                .keyFeatures(Arrays.asList("Bose ANC", "24-hour battery", "Aware mode",
                        "Multipoint Bluetooth", "Comfortable fit", "USB-C charging"))
                .notableHighlights("Most comfortable headphones, excellent ANC, great for long sessions")
                .rating(4.6).reviewCount(14200).imageUrl("https://m.media-amazon.com/images/I/61CGHv6kmWL._SL1500_.jpg")
                .sourceUrl("https://www.amazon.in/s?k=Bose+QuietComfort+45").platform("Mock")
                .availability("In Stock").discount("18% off").build()
        ));

        // Televisions
        MOCK_DB.put("television", Arrays.asList(
            ProductInfo.builder()
                .id("TV001").name("Samsung 65-inch QLED 4K Q80C").brand("Samsung").category("television")
                .price(89999.0).priceRange("₹85,000 - ₹95,000")
                .keyFeatures(Arrays.asList("65-inch QLED 4K", "Quantum HDR 1500", "120Hz refresh rate",
                        "Tizen OS", "4 HDMI ports", "Dolby Atmos", "Gaming Hub"))
                .notableHighlights("Excellent picture quality, great for gaming, smart TV features")
                .rating(4.6).reviewCount(11200).imageUrl("https://m.media-amazon.com/images/I/91MSp0P9IEL._SL1500_.jpg")
                .sourceUrl("https://www.amazon.in/s?k=Samsung+QLED+65+Q80C").platform("Mock")
                .availability("In Stock").discount("15% off").build(),

            ProductInfo.builder()
                .id("TV002").name("LG C3 OLED 55-inch").brand("LG").category("television")
                .price(129999.0).priceRange("₹1,20,000 - ₹1,40,000")
                .keyFeatures(Arrays.asList("55-inch OLED 4K", "α9 AI Processor Gen6", "120Hz refresh rate",
                        "webOS 23", "Dolby Vision IQ", "4 HDMI 2.1 ports", "G-Sync & FreeSync"))
                .notableHighlights("Best OLED TV, perfect blacks, best for gaming and movies")
                .rating(4.9).reviewCount(7800).imageUrl("https://m.media-amazon.com/images/I/81Ot9QXJKWL._SL1500_.jpg")
                .sourceUrl("https://www.flipkart.com/search?q=LG+C3+OLED+55").platform("Mock")
                .availability("In Stock").discount("10% off").build(),

            ProductInfo.builder()
                .id("TV003").name("Sony Bravia XR 55-inch").brand("Sony").category("television")
                .price(109999.0).priceRange("₹1,00,000 - ₹1,20,000")
                .keyFeatures(Arrays.asList("55-inch 4K OLED", "Cognitive Processor XR", "120Hz",
                        "Google TV", "Acoustic Surface Audio+", "HDMI 2.1"))
                .notableHighlights("Best sound quality in a TV, Google TV integration, XR processor")
                .rating(4.7).reviewCount(6300).imageUrl("https://m.media-amazon.com/images/I/71Sa3dqTqzL._SL1500_.jpg")
                .sourceUrl("https://www.amazon.in/s?k=Sony+Bravia+XR+55").platform("Mock")
                .availability("In Stock").discount("12% off").build()
        ));

        // Cameras
        MOCK_DB.put("camera", Arrays.asList(
            ProductInfo.builder()
                .id("CAM001").name("Sony Alpha A7 IV").brand("Sony").category("camera")
                .price(229990.0).priceRange("₹2,20,000 - ₹2,40,000")
                .keyFeatures(Arrays.asList("33MP full-frame BSI CMOS", "4K 60fps video",
                        "759 phase-detect AF points", "10fps burst", "5-axis IBIS", "Dual card slots"))
                .notableHighlights("Best all-around mirrorless camera, excellent for both photo and video")
                .rating(4.8).reviewCount(4200).imageUrl("https://m.media-amazon.com/images/I/71f5Eu5lJSL._SL1500_.jpg")
                .sourceUrl("https://www.amazon.in/s?k=Sony+Alpha+A7+IV").platform("Mock")
                .availability("In Stock").discount("5% off").build(),

            ProductInfo.builder()
                .id("CAM002").name("Canon EOS R6 Mark II").brand("Canon").category("camera")
                .price(219990.0).priceRange("₹2,10,000 - ₹2,30,000")
                .keyFeatures(Arrays.asList("24.2MP full-frame CMOS", "4K 60fps RAW video",
                        "40fps burst", "Dual Pixel CMOS AF II", "8-stop IBIS", "Weather sealed"))
                .notableHighlights("Best for sports and wildlife, incredible AF tracking, fast burst rate")
                .rating(4.7).reviewCount(3800).imageUrl("https://m.media-amazon.com/images/I/71Swqqe7XAL._SL1500_.jpg")
                .sourceUrl("https://www.amazon.in/s?k=Canon+EOS+R6+Mark+II").platform("Mock")
                .availability("In Stock").discount("8% off").build()
        ));

        // Tablets
        MOCK_DB.put("tablet", Arrays.asList(
            ProductInfo.builder()
                .id("TAB001").name("Apple iPad Pro 12.9-inch M2").brand("Apple").category("tablet")
                .price(112900.0).priceRange("₹1,05,000 - ₹1,20,000")
                .keyFeatures(Arrays.asList("Apple M2 chip", "12.9-inch Liquid Retina XDR",
                        "8GB RAM", "256GB storage", "Thunderbolt 4", "Apple Pencil 2 support", "5G capable"))
                .notableHighlights("Most powerful tablet, ProMotion display, desktop-class performance")
                .rating(4.8).reviewCount(9600).imageUrl("https://m.media-amazon.com/images/I/81+7Up7IWuL._SL1500_.jpg")
                .sourceUrl("https://www.amazon.in/s?k=iPad+Pro+M2").platform("Mock")
                .availability("In Stock").discount("").build(),

            ProductInfo.builder()
                .id("TAB002").name("Samsung Galaxy Tab S9+").brand("Samsung").category("tablet")
                .price(89999.0).priceRange("₹85,000 - ₹95,000")
                .keyFeatures(Arrays.asList("Snapdragon 8 Gen 2", "12GB RAM", "256GB storage",
                        "12.4-inch Dynamic AMOLED 2X", "S Pen included", "IP68 water resistant", "DeX mode"))
                .notableHighlights("Best Android tablet, included S Pen, DeX desktop mode")
                .rating(4.7).reviewCount(7400).imageUrl("https://m.media-amazon.com/images/I/71Vy4KCQKZL._SL1500_.jpg")
                .sourceUrl("https://www.flipkart.com/search?q=Samsung+Galaxy+Tab+S9+Plus").platform("Mock")
                .availability("In Stock").discount("10% off").build()
        ));

        // Refrigerators
        MOCK_DB.put("refrigerator", Arrays.asList(
            ProductInfo.builder()
                .id("REF001").name("Samsung 653L French Door Refrigerator").brand("Samsung").category("refrigerator")
                .price(89990.0).priceRange("₹85,000 - ₹95,000")
                .keyFeatures(Arrays.asList("653L capacity", "French door design", "Twin Cooling Plus",
                        "Digital Inverter Compressor", "SpaceMax technology", "Wi-Fi connectivity"))
                .notableHighlights("Best large family refrigerator, energy efficient, smart features")
                .rating(4.5).reviewCount(5600).imageUrl("https://m.media-amazon.com/images/I/61CGHv6kmWL._SL1500_.jpg")
                .sourceUrl("https://www.amazon.in/s?k=Samsung+French+Door+Refrigerator").platform("Mock")
                .availability("In Stock").discount("15% off").build(),

            ProductInfo.builder()
                .id("REF002").name("LG 655L Side-by-Side Refrigerator").brand("LG").category("refrigerator")
                .price(79990.0).priceRange("₹75,000 - ₹85,000")
                .keyFeatures(Arrays.asList("655L capacity", "Side-by-side design", "InstaView Door-in-Door",
                        "Linear Cooling", "Smart Diagnosis", "Multi Air Flow"))
                .notableHighlights("InstaView window, excellent cooling technology, smart features")
                .rating(4.4).reviewCount(4800).imageUrl("https://m.media-amazon.com/images/I/71Sa3dqTqzL._SL1500_.jpg")
                .sourceUrl("https://www.flipkart.com/search?q=LG+Side+by+Side+Refrigerator").platform("Mock")
                .availability("In Stock").discount("18% off").build()
        ));

        // Washing Machines
        MOCK_DB.put("washing machine", Arrays.asList(
            ProductInfo.builder()
                .id("WM001").name("LG 9kg Front Load Washing Machine").brand("LG").category("washing machine")
                .price(54990.0).priceRange("₹50,000 - ₹60,000")
                .keyFeatures(Arrays.asList("9kg capacity", "AI DD technology", "Steam wash",
                        "TurboWash 360", "Wi-Fi connectivity", "1400 RPM", "6 Motion Direct Drive"))
                .notableHighlights("Best front load washer, AI fabric detection, steam cleaning")
                .rating(4.6).reviewCount(8900).imageUrl("https://m.media-amazon.com/images/I/81Ot9QXJKWL._SL1500_.jpg")
                .sourceUrl("https://www.amazon.in/s?k=LG+Front+Load+Washing+Machine+9kg").platform("Mock")
                .availability("In Stock").discount("12% off").build(),

            ProductInfo.builder()
                .id("WM002").name("Samsung 8kg Front Load Washing Machine").brand("Samsung").category("washing machine")
                .price(44990.0).priceRange("₹40,000 - ₹50,000")
                .keyFeatures(Arrays.asList("8kg capacity", "EcoBubble technology", "Digital Inverter Motor",
                        "QuickDrive", "Wi-Fi SmartThings", "1400 RPM"))
                .notableHighlights("EcoBubble for gentle yet effective cleaning, energy efficient")
                .rating(4.5).reviewCount(7200).imageUrl("https://m.media-amazon.com/images/I/71f5Eu5lJSL._SL1500_.jpg")
                .sourceUrl("https://www.flipkart.com/search?q=Samsung+Front+Load+Washing+Machine+8kg").platform("Mock")
                .availability("In Stock").discount("20% off").build()
        ));

        // Air Conditioners
        MOCK_DB.put("air conditioner", Arrays.asList(
            ProductInfo.builder()
                .id("AC001").name("Daikin 1.5 Ton 5 Star Inverter AC").brand("Daikin").category("air conditioner")
                .price(44990.0).priceRange("₹42,000 - ₹48,000")
                .keyFeatures(Arrays.asList("1.5 Ton capacity", "5 Star BEE rating", "Inverter compressor",
                        "PM 2.5 filter", "Auto-clean", "Wi-Fi enabled", "Coanda airflow"))
                .notableHighlights("Most energy efficient AC, best after-sales service, reliable brand")
                .rating(4.7).reviewCount(13400).imageUrl("https://m.media-amazon.com/images/I/71xb2xkN5qL._SL1500_.jpg")
                .sourceUrl("https://www.amazon.in/s?k=Daikin+1.5+Ton+5+Star+Inverter+AC").platform("Mock")
                .availability("In Stock").discount("10% off").build(),

            ProductInfo.builder()
                .id("AC002").name("LG 1.5 Ton 5 Star DUAL Inverter AC").brand("LG").category("air conditioner")
                .price(42990.0).priceRange("₹40,000 - ₹46,000")
                .keyFeatures(Arrays.asList("1.5 Ton capacity", "5 Star BEE rating", "DUAL Inverter compressor",
                        "HD filter with anti-virus protection", "Auto clean", "Wi-Fi ThinQ"))
                .notableHighlights("DUAL Inverter for faster cooling, anti-virus filter, smart control")
                .rating(4.6).reviewCount(11800).imageUrl("https://m.media-amazon.com/images/I/61bK6PMOC3L._SL1500_.jpg")
                .sourceUrl("https://www.flipkart.com/search?q=LG+1.5+Ton+5+Star+Inverter+AC").platform("Mock")
                .availability("In Stock").discount("15% off").build()
        ));

        // Microwave
        MOCK_DB.put("microwave", Arrays.asList(
            ProductInfo.builder()
                .id("MW001").name("LG 32L Convection Microwave Oven").brand("LG").category("microwave")
                .price(19990.0).priceRange("₹18,000 - ₹22,000")
                .keyFeatures(Arrays.asList("32L capacity", "Convection + Grill + Microwave",
                        "Diet Fry", "Auto Cook Menu", "Charcoal Lighting Heater", "Wi-Fi enabled"))
                .notableHighlights("Best convection microwave, diet fry feature, large capacity")
                .rating(4.5).reviewCount(8700).imageUrl("https://m.media-amazon.com/images/I/71Swqqe7XAL._SL1500_.jpg")
                .sourceUrl("https://www.amazon.in/s?k=LG+32L+Convection+Microwave").platform("Mock")
                .availability("In Stock").discount("20% off").build()
        ));
    }

    private List<ProductInfo> getMockProducts(String category, Double budget, String brand) {
        String normalizedCategory = category.toLowerCase().trim();

        List<ProductInfo> products = MOCK_DB.entrySet().stream()
                .filter(e -> normalizedCategory.contains(e.getKey()) ||
                             e.getKey().contains(normalizedCategory))
                .flatMap(e -> e.getValue().stream())
                .collect(Collectors.toList());

        if (products.isEmpty()) {
            products = generateGenericProducts(category, budget);
        }
        return products;
    }

    private List<ProductInfo> generateGenericProducts(String category, Double budget) {
        List<ProductInfo> generic = new ArrayList<>();
        String[] brands = {"Samsung", "LG", "Sony", "Philips", "Bosch"};
        for (int i = 1; i <= 3; i++) {
            double price = budget != null ? budget * (0.7 + (i * 0.1)) : 10000.0 * i;
            generic.add(ProductInfo.builder()
                    .id("GEN00" + i)
                    .name(brands[i - 1] + " " + category + " Model " + (2024 + i))
                    .brand(brands[i - 1])
                    .category(category)
                    .price(price)
                    .priceRange("₹" + (long) (price * 0.9) + " - ₹" + (long) (price * 1.1))
                    .keyFeatures(Arrays.asList("Feature 1", "Feature 2", "Feature 3",
                            "Energy Efficient", "1 Year Warranty"))
                    .notableHighlights("Good value for money product in " + category + " category")
                    .rating(4.0 + (i * 0.1))
                    .reviewCount(1000 * i)
                    .imageUrl("https://via.placeholder.com/300x300?text=" + category.replace(" ", "+"))
                    .sourceUrl("https://www.amazon.in/s?k=" + encodeParam(category))
                    .platform("Mock")
                    .availability("In Stock")
                    .discount("")
                    .build());
        }
        return generic;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private String buildQuery(String category, String brand) {
        if (brand != null && !brand.isBlank()) {
            return brand + " " + category;
        }
        return category;
    }

    private String encodeParam(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value.replace(" ", "+");
        }
    }

    private Double parsePrice(String priceStr) {
        if (priceStr == null || priceStr.isBlank()) return null;
        String cleaned = priceStr.replaceAll("[^0-9.]", "");
        if (cleaned.isBlank()) return null;
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String extractBrand(String productTitle) {
        if (productTitle == null || productTitle.isBlank()) return "Unknown";
        String[] knownBrands = {"Apple", "Samsung", "Sony", "LG", "Dell", "HP", "Lenovo",
                "ASUS", "Acer", "OnePlus", "Xiaomi", "Realme", "Oppo", "Vivo", "Motorola",
                "Bose", "JBL", "Sennheiser", "Canon", "Nikon", "Fujifilm", "Daikin",
                "Voltas", "Whirlpool", "Bosch", "IFB", "Panasonic", "Philips"};
        for (String brand : knownBrands) {
            if (productTitle.toLowerCase().contains(brand.toLowerCase())) {
                return brand;
            }
        }
        // Return first word as brand
        return productTitle.split("\\s+")[0];
    }
}
