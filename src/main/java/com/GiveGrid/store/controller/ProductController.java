package com.GiveGrid.store.controller;

import com.GiveGrid.store.entity.User;
import com.GiveGrid.store.entity.Product;
import com.GiveGrid.store.entity.ProductImage;
import com.GiveGrid.store.repository.ProductImageRepository;
import com.GiveGrid.store.service.ProductService;
import com.GiveGrid.store.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductImageRepository productImageRepository;

    // Show add product page
    @GetMapping("/products/add")
    public String addProduct(Model model) {
        model.addAttribute("product", new Product());
        return "add-product";
    }

    // Handle POST request for saving product
    @PostMapping("/products/add")
    public String saveNewProduct(@ModelAttribute("product") Product product,
                                  @RequestParam(name = "images", required = false) List<MultipartFile> images) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        User seller = userService.findByUsername(username);

        if (seller == null) {
            return "redirect:/login";
        }

        product.setSeller(seller);
        Product savedProduct = productService.saveProduct(product);
        saveImages(savedProduct, images);

        return "success-product";
    }

    // Show only the seller's products
    @GetMapping("/products")
    public String listProducts(Model model, Authentication auth) {

        User currentUser = null;
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            currentUser = userService.findByUsername(auth.getName());
        }

        if (currentUser != null && "SELLER".equalsIgnoreCase(currentUser.getRole())) {
            model.addAttribute("products", productService.getProductsBySeller(currentUser));
        } else {
            model.addAttribute("products", productService.getAllProducts());
        }
        model.addAttribute("user", currentUser);
        return "products";
    }

    // View a single product
    @GetMapping("/products/view/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/products";
        }
        model.addAttribute("product", product);
        model.addAttribute("images", productImageRepository.findByProductIdOrderByIdAsc(id));
        return "view-product";
    }

    // SEARCH — in-memory filtering
    @GetMapping("/search")
    public String searchProducts(@RequestParam(name = "query", required = false) String query,
                                 Model model) {

        if (query == null || query.trim().isEmpty()) {
            model.addAttribute("results", List.of());
            model.addAttribute("query", "");
            return "search-results";
        }

        String q = query.toLowerCase(Locale.ROOT).trim();

        List<Product> results = productService.getAllProducts()
                .stream()
                .filter(p -> {
                    String name = p.getName() != null ? p.getName().toLowerCase() : "";
                    String desc = p.getDescription() != null ? p.getDescription().toLowerCase() : "";
                    return name.contains(q) || desc.contains(q);
                })
                .collect(Collectors.toList());

        model.addAttribute("results", results);
        model.addAttribute("query", query);
        return "search-results";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, Authentication auth) {

        if (auth == null) return "redirect:/login";

        String username = auth.getName();
        User seller = userService.findByUsername(username);

        Product product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/products?notfound";
        }

        // Make sure only the owner can delete
        if (!product.getSeller().getId().equals(seller.getId())) {
            return "redirect:/products?unauthorized";
        }

        productService.deleteProduct(id);

        return "redirect:/products?deleted";   // <-- Redirect after deletion
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Authentication auth, Model model) {

        if (auth == null) return "redirect:/login";

        String username = auth.getName();
        Product product = productService.getProductById(id);

        // Product does not exist
        if (product == null) return "redirect:/products";

        // Only the seller who created it can edit
        if (!product.getSeller().getUsername().equals(username)) {
            return "redirect:/products"; // Unauthorized access
        }

        model.addAttribute("product", product);
        return "edit-product";
    }

    @PostMapping("/products/edit/{id}")
    public String updateProduct(
            @PathVariable Long id,
            @ModelAttribute("product") Product updatedProduct,
            @RequestParam(name = "images", required = false) List<MultipartFile> images,
            Authentication auth
    ) {
        if (auth == null) return "redirect:/login";

        Product existing = productService.getProductById(id);

        if (existing == null) return "redirect:/products";

        // Check ownership
        if (!existing.getSeller().getUsername().equals(auth.getName())) {
            return "redirect:/products"; // Unauthorized
        }

        // Update fields
        existing.setName(updatedProduct.getName());
        existing.setDescription(updatedProduct.getDescription());
        existing.setQuantity(updatedProduct.getQuantity());
        existing.setCondition(updatedProduct.getCondition());  // ← FIX HERE

        productService.saveProduct(existing);
        saveImages(existing, images);

        return "redirect:/products?updated=true";
    }

    @GetMapping("/products/{productId}/images/{imageId}")
    @ResponseBody
    public org.springframework.http.ResponseEntity<byte[]> getProductImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {

        ProductImage image = productImageRepository
                .findByIdAndProductId(imageId, productId)
                .orElse(null);

        if (image == null) {
            return org.springframework.http.ResponseEntity.notFound().build();
        }

        return org.springframework.http.ResponseEntity.ok()
                .header("Cache-Control", "public, max-age=86400")
                .header("Content-Type", image.getContentType())
                .body(image.getImageData());
    }

    private void saveImages(Product product, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return;
        }

        int existingCount = productImageRepository.findByProductIdOrderByIdAsc(product.getId()).size();
        int remaining = Math.max(0, 4 - existingCount);

        for (MultipartFile file : images) {
            if (remaining == 0) {
                break;
            }
            if (file == null || file.isEmpty() || file.getSize() > 5 * 1024 * 1024) {
                continue;
            }

            String contentType = file.getContentType();
            if (!List.of("image/jpeg", "image/png", "image/gif").contains(contentType)) {
                continue;
            }

            try {
                byte[] bytes = file.getBytes();
                if (ImageIO.read(new ByteArrayInputStream(bytes)) == null) {
                    continue;
                }

                ProductImage image = new ProductImage();
                image.setProduct(product);
                image.setImageData(bytes);
                image.setContentType(contentType);
                productImageRepository.save(image);
                remaining--;
            } catch (Exception ignored) {
                // Skip an invalid image without preventing the request from being saved.
            }
        }
    }
}
