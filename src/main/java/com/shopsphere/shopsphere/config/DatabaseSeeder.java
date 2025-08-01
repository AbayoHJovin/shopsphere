package com.shopsphere.shopsphere.config;

import com.shopsphere.shopsphere.enums.Gender;
import com.shopsphere.shopsphere.enums.OrderPaymentStatus;
import com.shopsphere.shopsphere.enums.OrderStatus;
import com.shopsphere.shopsphere.enums.Role;
import com.shopsphere.shopsphere.enums.Size;
import com.shopsphere.shopsphere.models.*;
import com.shopsphere.shopsphere.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductColorRepository productColorRepository;
    private final ProductSizeRepository productSizeRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderTransactionRepository orderTransactionRepository;

    public DatabaseSeeder(UserRepository userRepository, 
                         PasswordEncoder passwordEncoder,
                         ProductRepository productRepository,
                         CategoryRepository categoryRepository,
                         ProductColorRepository productColorRepository,
                         ProductSizeRepository productSizeRepository,
                         OrderRepository orderRepository,
                         OrderItemRepository orderItemRepository,
                         OrderTransactionRepository orderTransactionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productColorRepository = productColorRepository;
        this.productSizeRepository = productSizeRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderTransactionRepository = orderTransactionRepository;
    }

    @Override
    public void run(String... args) {
        // Seed admin user
        User admin = seedAdminUser();
        
        // Seed categories
        List<Category> categories = seedCategories();
        
        // Seed colors
        List<ProductColor> colors = seedColors();
        
        // Seed products
        List<Product> products = seedProducts(categories, colors);
        
        // Seed orders
        seedOrders(admin, products);
    }
    
    private User seedAdminUser() {
        // Check if admin user exists by email
        String adminEmail = "abayohirwajovin@gmail.com";
        Optional<User> existingAdmin = userRepository.findByEmail(adminEmail);
        
        if (existingAdmin.isEmpty()) {
            // Create new admin user
            User admin = new User();
            // Let JPA/Hibernate generate the ID
            admin.setEmail(adminEmail);
            admin.setUsername("ABAYO HIRWA Jovin");
            admin.setPassword(passwordEncoder.encode("JOVIN19"));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());
            admin.setProfilePicture(null); // No profile picture specified

            // Save to database
            admin = userRepository.save(admin);
            System.out.println("Admin user created: " + adminEmail);
            return admin;
        } else {
            System.out.println("Admin user already exists: " + adminEmail);
            return existingAdmin.get();
        }
    }
    
    private List<Category> seedCategories() {
        // Check if categories already exist
        if (categoryRepository.count() > 0) {
            System.out.println("Categories already exist, skipping seeding");
            return categoryRepository.findAll();
        }
        
        // Create categories
        List<Category> categories = new ArrayList<>();
        
        Category clothing = Category.builder()
                .name("Clothing")
                .description("All types of clothing items")
                .build();
        
        Category electronics = Category.builder()
                .name("Electronics")
                .description("Electronic devices and accessories")
                .build();
        
        Category homeDecor = Category.builder()
                .name("Home Decor")
                .description("Items to decorate your home")
                .build();
        
        categories.add(categoryRepository.save(clothing));
        categories.add(categoryRepository.save(electronics));
        categories.add(categoryRepository.save(homeDecor));
        
        System.out.println("Categories seeded: " + categories.size());
        return categories;
    }
    
    private List<ProductColor> seedColors() {
        // Check if colors already exist
        if (productColorRepository.count() > 0) {
            System.out.println("Colors already exist, skipping seeding");
            return productColorRepository.findAll();
        }
        
        // Create colors
        List<ProductColor> colors = new ArrayList<>();
        
        ProductColor red = ProductColor.builder()
                .colorName("Red")
                .colorHexCode("#FF0000")
                .build();
        
        ProductColor blue = ProductColor.builder()
                .colorName("Blue")
                .colorHexCode("#0000FF")
                .build();
        
        ProductColor green = ProductColor.builder()
                .colorName("Green")
                .colorHexCode("#00FF00")
                .build();
        
        ProductColor black = ProductColor.builder()
                .colorName("Black")
                .colorHexCode("#000000")
                .build();
        
        ProductColor white = ProductColor.builder()
                .colorName("White")
                .colorHexCode("#FFFFFF")
                .build();
        
        colors.add(productColorRepository.save(red));
        colors.add(productColorRepository.save(blue));
        colors.add(productColorRepository.save(green));
        colors.add(productColorRepository.save(black));
        colors.add(productColorRepository.save(white));
        
        System.out.println("Colors seeded: " + colors.size());
        return colors;
    }
    
    private List<Product> seedProducts(List<Category> categories, List<ProductColor> colors) {
        // Check if we already have at least 3 products
        if (productRepository.count() >= 3) {
            System.out.println("At least 3 products already exist, skipping seeding");
            return productRepository.findAll();
        }
        
        // Ensure we have at least one category
        if (categories.isEmpty()) {
            Category defaultCategory = Category.builder()
                    .name("General")
                    .description("General category for all products")
                    .build();
            categories = List.of(categoryRepository.save(defaultCategory));
        }
        
        // Create 3 random products
        List<Product> products = new ArrayList<>();
        
        // Product 1: T-Shirt
        Product tShirt = Product.builder()
                .name("Premium Cotton T-Shirt")
                .description("High-quality cotton t-shirt, comfortable for everyday wear")
                .price(new BigDecimal("29.99"))
                .gender(Gender.UNISEX)
                .stock(100)
                .popular(true)
                .categories(List.of(categories.get(0))) // First available category
                .colors(getRandomSublist(colors, Math.min(3, colors.size())))
                .build();
        
        // Save product first to get ID
        tShirt = productRepository.save(tShirt);
        
        // Add sizes with stock
        addSizesToProduct(tShirt, Size.SMALL, 30);
        addSizesToProduct(tShirt, Size.MEDIUM, 40);
        addSizesToProduct(tShirt, Size.LARGE, 30);
        
        products.add(tShirt);
        
        // Only create second product if we have at least 2 categories
        if (categories.size() > 1) {
            // Product 2: Smartphone
            Product smartphone = Product.builder()
                    .name("SmartTech Pro Phone")
                    .description("Latest smartphone with advanced camera and long battery life")
                    .price(new BigDecimal("899.99"))
                    .gender(Gender.UNISEX)
                    .stock(50)
                    .popular(true)
                    .categories(List.of(categories.get(1))) // Second category
                    .colors(getRandomSublist(colors, Math.min(2, colors.size())))
                    .build();
            
            smartphone = productRepository.save(smartphone);
            products.add(smartphone);
        }
        
        // Only create third product if we have at least 3 categories
        if (categories.size() > 2) {
            // Product 3: Decorative Lamp
            Product lamp = Product.builder()
                    .name("Modern LED Table Lamp")
                    .description("Elegant table lamp with adjustable brightness, perfect for any room")
                    .price(new BigDecimal("79.99"))
                    .gender(Gender.UNISEX)
                    .stock(75)
                    .popular(false)
                    .categories(List.of(categories.get(2))) // Third category
                    .colors(getRandomSublist(colors, Math.min(3, colors.size())))
                    .build();
            
            lamp = productRepository.save(lamp);
            products.add(lamp);
        }
        // Products are already added in their respective if blocks
        System.out.println("Products seeded: " + products.size());
        return products;
    }
    
    private void addSizesToProduct(Product product, Size size, int stock) {
        ProductSize productSize = ProductSize.builder()
                .product(product)
                .size(size)
                .stockForSize(stock)
                .build();
        
        productSizeRepository.save(productSize);
    }
    
    private <T> List<T> getRandomSublist(List<T> list, int size) {
        Collections.shuffle(list);
        return list.stream().limit(size).collect(Collectors.toList());
    }
    
    private void seedOrders(User user, List<Product> products) {
        // Check if orders already exist
        if (orderRepository.count() > 0) {
            System.out.println("Orders already exist, skipping seeding");
            return;
        }
        
        // Create 3 orders with different statuses
        // Order 1: Delivered order
        createOrder(
            user,
            products,
            OrderStatus.DELIVERED,
            OrderPaymentStatus.PAID,
            LocalDateTime.now().minusDays(30),
            "ORD-" + generateRandomOrderCode(),
            true
        );
        
        // Order 2: Processing order
        createOrder(
            user,
            products,
            OrderStatus.PROCESSING,
            OrderPaymentStatus.PAID,
            LocalDateTime.now().minusDays(5),
            "ORD-" + generateRandomOrderCode(),
            false
        );
        
        // Order 3: Pending order
        createOrder(
            user,
            products,
            OrderStatus.PENDING,
            OrderPaymentStatus.PENDING,
            LocalDateTime.now().minusHours(12),
            "ORD-" + generateRandomOrderCode(),
            false
        );
        
        System.out.println("Orders seeded: 3");
    }
    
    private void createOrder(User user, List<Product> products, OrderStatus status, 
                            OrderPaymentStatus paymentStatus, LocalDateTime orderDate, 
                            String orderCode, boolean isQrScanned) {
        // Create random order items (1-3 items)
        Random random = new Random();
        int numItems = random.nextInt(3) + 1;
        
        // Calculate total amount based on selected products
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        
        // Create the order
        Order order = Order.builder()
                .orderCode(orderCode)
                .orderStatus(status)
                .paymentStatus(paymentStatus)
                .orderDate(orderDate)
                .updatedAt(orderDate.plusHours(2))
                .totalAmount(BigDecimal.ZERO) // Will update after adding items
                .shippingCost(new BigDecimal("10.00"))
                .taxAmount(new BigDecimal("0.00"))
                .discountAmount(new BigDecimal("0.00"))
                .isQrScanned(isQrScanned)
                .user(user)
                .email(user.getEmail())
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+1234567890")
                .streetAddress("123 Main St")
                .city("New York")
                .stateProvince("NY")
                .postalCode("10001")
                .country("USA")
                .notes("Please deliver to the front door")
                .build();
        
        order = orderRepository.save(order);
        
        // Add order items
        for (int i = 0; i < numItems; i++) {
            Product product = products.get(random.nextInt(products.size()));
            int quantity = random.nextInt(3) + 1;
            
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(quantity)
                    .price(product.getPrice())
                    .createdAt(orderDate)
                    .build();
            
            orderItemRepository.save(orderItem);
            orderItems.add(orderItem);
            
            // Add to total
            totalAmount = totalAmount.add(product.getPrice().multiply(new BigDecimal(quantity)));
        }
        
        // Update order with total amount
        order.setTotalAmount(totalAmount.add(order.getShippingCost()));
        order = orderRepository.save(order);
        
        // Create transaction if paid
        if (paymentStatus == OrderPaymentStatus.PAID) {
            OrderTransaction transaction = OrderTransaction.builder()
                    .order(order)
                    .amount(order.getTotalAmount())
                    .paymentMethod("Credit Card")
                    .transactionReference("TXN-" + UUID.randomUUID().toString().substring(0, 8))
                    .status(OrderPaymentStatus.PAID)
                    .transactionDate(orderDate.plusMinutes(30))
                    .createdAt(orderDate.plusMinutes(30))
                    .build();
            
            orderTransactionRepository.save(transaction);
        }
    }
    
    private String generateRandomOrderCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}