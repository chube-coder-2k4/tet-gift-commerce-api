package com.tetgift.component;

import com.tetgift.model.*;
import com.tetgift.model.entity.ProductEntity;
import com.tetgift.repository.jpa.AddressRepository;
import com.tetgift.repository.jpa.CategoryRepository;
import com.tetgift.repository.jpa.ProductRepository;
import com.tetgift.repository.jpa.RoleRepository;
import com.tetgift.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedRoles();
        seedUsers();
        seedCategories();
        seedProducts();
        seedAddresses();
    }

    private void seedRoles() {
        if (roleRepository.count() == 0) {
            List<Role> roles = new ArrayList<>();
            roles.add(Role.builder().name("ADMIN").description("Administrator with full access").build());
            roles.add(Role.builder().name("USER").description("Regular user with limited access").build());
            roleRepository.saveAll(roles);
            System.out.println("Seeded Roles");
        }
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
            Role userRole = roleRepository.findByName("USER").orElseThrow();

            List<Users> users = new ArrayList<>();

            // Admin user
            users.add(Users.builder()
                    .fullName("System Admin")
                    .email("admin@tetgift.com")
                    .password(passwordEncoder.encode("admin123"))
                    .phone("0900000001")
                    .username("admin")
                    .role(adminRole)
                    .isVerify(true)
                    .isActive(true)
                    .build());

            // Regular users
            for (int i = 1; i <= 5; i++) {
                users.add(Users.builder()
                        .fullName("User " + i)
                        .email("user" + i + "@tetgift.com")
                        .password(passwordEncoder.encode("user123")) // Same password for testing
                        .phone("090000000" + (i + 1))
                        .username("user" + i)
                        .role(userRole)
                        .isVerify(true)
                        .isActive(true)
                        .build());
            }
            userRepository.saveAll(users);
            System.out.println("Seeded Users");
        }
    }

    private void seedCategories() {
        if (categoryRepository.count() == 0) {
            List<Category> categories = new ArrayList<>();
            categories.add(Category.builder().name("Giỏ Quà Tết")
                    .description("Các loại giỏ quà tết sang trọng")
                    .isActive(true)
                    .build());
            categories.add(Category.builder().name("Hộp Quà Cao Cấp")
                    .description("Hộp quà thiết kế tinh tế")
                    .isActive(true)
                    .build());
            categories.add(Category.builder().name("Rượu Vang")
                    .description("Các loại rượu vang nhập khẩu")
                    .isActive(true)
                    .build());
            categories.add(Category.builder().name("Bánh Kẹo Tết")
                    .description("Bánh kẹo truyền thống và hiện đại")
                    .isActive(true)
                    .build());
            categories.add(Category.builder().name("Hạt Dinh Dưỡng")
                    .description("Các loại hạt tốt cho sức khỏe")
                    .isActive(true)
                    .build());
            categoryRepository.saveAll(categories);
            System.out.println("Seeded Categories");
        }
    }

    private void seedProducts() {
        if (productRepository.count() == 0) {
            List<Category> allCategories = categoryRepository.findAll();
            if (allCategories.isEmpty()) return;

            Category gioQua = allCategories.stream().filter(c -> c.getName().equals("Giỏ Quà Tết")).findFirst().orElse(allCategories.get(0));
            Category ruouVang = allCategories.stream().filter(c -> c.getName().equals("Rượu Vang")).findFirst().orElse(allCategories.get(0));
            Category banhKeo = allCategories.stream().filter(c -> c.getName().equals("Bánh Kẹo Tết")).findFirst().orElse(allCategories.get(0));

            List<ProductEntity> products = new ArrayList<>();

            // Products for 'Giỏ Quà Tết'
            products.add(ProductEntity.builder()
                    .name("Giỏ Quà Tết An Khang")
                    .description("Giỏ quà tết bao gồm bánh, kẹo, rượu vang, trà.")
                    .price(new BigDecimal("1500000"))
                    .stock(100)
                    .category(gioQua)
                    .isActive(true)
                    .manufactureDate(LocalDate.now().minusMonths(1))
                    .expDate(LocalDate.now().plusMonths(6))
                    .image("https://res.cloudinary.com/dfupyxrmr/image/upload/v1/products/sample1")
                    .build());

            products.add(ProductEntity.builder()
                    .name("Giỏ Quà Tết Thịnh Vượng")
                    .description("Giỏ quà cao cấp với yến sào và đông trùng hạ thảo.")
                    .price(new BigDecimal("2500000"))
                    .stock(50)
                    .category(gioQua)
                    .isActive(true)
                    .manufactureDate(LocalDate.now().minusMonths(1))
                    .expDate(LocalDate.now().plusMonths(12))
                    .image("https://res.cloudinary.com/dfupyxrmr/image/upload/v1/products/sample2")
                    .build());

            // Products for 'Rượu Vang'
            products.add(ProductEntity.builder()
                    .name("Rượu Vang Chile")
                    .description("Rượu vang đỏ nhập khẩu từ Chile.")
                    .price(new BigDecimal("500000"))
                    .stock(200)
                    .category(ruouVang)
                    .isActive(true)
                    .manufactureDate(LocalDate.now().minusYears(2))
                    .expDate(LocalDate.now().plusYears(10))
                    .image("https://res.cloudinary.com/dfupyxrmr/image/upload/v1/products/sample3")
                    .build());

            products.add(ProductEntity.builder()
                    .name("Rượu Vang Pháp Bordeaux")
                    .description("Hương vị đậm đà, sang trọng.")
                    .price(new BigDecimal("1200000"))
                    .stock(120)
                    .category(ruouVang)
                    .isActive(true)
                    .manufactureDate(LocalDate.now().minusYears(3))
                    .expDate(LocalDate.now().plusYears(15))
                    .image("https://res.cloudinary.com/dfupyxrmr/image/upload/v1/products/sample4")
                    .build());

            // Products for 'Bánh Kẹo Tết'
            products.add(ProductEntity.builder()
                    .name("Hộp Bánh Quy Bơ")
                    .description("Bánh quy bơ thơm ngon, giòn rụm.")
                    .price(new BigDecimal("250000"))
                    .stock(500)
                    .category(banhKeo)
                    .isActive(true)
                    .manufactureDate(LocalDate.now().minusMonths(1))
                    .expDate(LocalDate.now().plusMonths(12))
                    .image("https://res.cloudinary.com/dfupyxrmr/image/upload/v1/products/sample5")
                    .build());

            products.add(ProductEntity.builder()
                    .name("Kẹo Socola Tổng Hợp")
                    .description("Đa dạng hương vị socola.")
                    .price(new BigDecimal("300000"))
                    .stock(300)
                    .category(banhKeo)
                    .isActive(true)
                    .manufactureDate(LocalDate.now().minusMonths(1))
                    .expDate(LocalDate.now().plusMonths(12))
                    .image("https://res.cloudinary.com/dfupyxrmr/image/upload/v1/products/sample6")
                    .build());

            productRepository.saveAll(products);
            System.out.println("Seeded Products");
        }
    }


    private void seedAddresses() {
        if (addressRepository.count() == 0) {
            List<Users> allUsers = userRepository.findAll();
            if (allUsers.isEmpty()) return;

            List<Address> addresses = new ArrayList<>();

            // Skip Admin, seed for regular users
            for (Users user : allUsers) {
                if (user.getRole() != null && "ADMIN".equals(user.getRole().getName())) continue;

                addresses.add(Address.builder()
                        .user(user)
                        .receiverName(user.getFullName())
                        .phone(user.getPhone())
                        .addressDetail("123 Đường Nguyễn Văn Linh, Quận 7, TP.HCM")
                        .isDefault(true)
                        .build());

                addresses.add(Address.builder()
                        .user(user)
                        .receiverName(user.getFullName() + " (Company)")
                        .phone(user.getPhone())
                        .addressDetail("456 Đường Lê Duẩn, Quận 1, TP.HCM")
                        .isDefault(false)
                        .build());
            }
            if (!addresses.isEmpty()) {
                addressRepository.saveAll(addresses);
                System.out.println("Seeded Addresses");
            }
        }
    }
}
