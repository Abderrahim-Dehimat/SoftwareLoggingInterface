package org.example;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * MainCLI is the entry point for the Command Line Interface (CLI) application that interacts with
 * a backend API for managing users and products. The class provides authentication, menu navigation,
 * and operations for both users and products through RESTful API calls.
 */

public class MainCLI {

    // Base URL of the backend API
    private static final String API_BASE_URL = "http://localhost:8080/api";
    // HttpClient instance for making RESTful API calls
    private static final HttpClient client = HttpClient.newHttpClient();
    // ObjectMapper for serializing and deserializing JSON data
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Authentication state variables
    private static boolean isAuthenticated = false;
    private static String authenticatedUserEmail = null;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Welcome to the Backend CLI ===");

        while (true) {
            if (!isAuthenticated) {
                authenticateUser(scanner);
            } else {
                System.out.println("\nMain Menu:");
                System.out.println("1. Manage Users");
                System.out.println("2. Manage Products");
                System.out.println("0. Exit");
                System.out.print("Enter your choice: ");
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1 -> manageUsers(scanner);
                    case 2 -> manageProducts(scanner);
                    case 0 -> {
                        System.out.println("Goodbye!");
                        return;
                    }
                    default -> System.out.println("Invalid choice! Please try again.");
                }
            }
        }
    }

    private static void authenticateUser(Scanner scanner) {
        System.out.println("Please log in to access the system.");
        System.out.print("Enter your email: ");
        String email = scanner.nextLine();
        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/users/authenticate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(Map.of(
                            "email", email,
                            "password", password
                    ))))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 && Boolean.parseBoolean(response.body())) {
                isAuthenticated = true;
                authenticatedUserEmail = email;
                System.out.println("Login successful! Welcome, " + email);
            } else {
                System.out.println("Invalid email or password. Please try again.");
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error during authentication: " + e.getMessage());
        }
    }

    private static void manageUsers(Scanner scanner) {
        System.out.println("\n=== Manage Users ===");
        System.out.println("1. Create User");
        System.out.println("2. Display All Users");
        System.out.println("0. Back to Main Menu");
        System.out.print("Enter your choice: ");
        int choice = Integer.parseInt(scanner.nextLine());

        switch (choice) {
            case 1 -> createUser(scanner);
            case 2 -> displayUsers();
            case 0 -> System.out.println("Returning to Main Menu...");
            default -> System.out.println("Invalid choice!");
        }
    }

    private static void createUser(Scanner scanner) {
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Age: ");
        int age = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter Email: ");
        String email = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/users/createUser"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(Map.of(
                            "name", name,
                            "age", age,
                            "email", email,
                            "password", password
                    ))))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("User created successfully!");
            } else {
                System.out.println("Failed to create user. Error: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error during user creation: " + e.getMessage());
        }
    }

    private static void displayUsers() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/users/readAllUsers"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                List<?> users = objectMapper.readValue(response.body(), List.class);
                System.out.println("=== Users ===");
                users.forEach(System.out::println);
            } else {
                System.out.println("Failed to fetch users. Error: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error during fetching users: " + e.getMessage());
        }
    }

    private static void manageProducts(Scanner scanner) {
        System.out.println("\n=== Manage Products ===");
        System.out.println("1. Display All Products");
        System.out.println("2. Fetch Product by ID");
        System.out.println("3. Add Product");
        System.out.println("4. Update Product");
        System.out.println("5. Delete Product");
        System.out.println("6. View 3 Most Expensive Products");
        System.out.println("0. Back to Main Menu");
        System.out.print("Enter your choice: ");
        int choice = Integer.parseInt(scanner.nextLine());

        switch (choice) {
            case 1 -> displayProducts();
            case 2 -> fetchProductById(scanner);
            case 3 -> addProduct(scanner);
            case 4 -> updateProduct(scanner);
            case 5 -> deleteProduct(scanner);
            case 6 -> viewTopExpensiveProducts();
            case 0 -> System.out.println("Returning to Main Menu...");
            default -> System.out.println("Invalid choice!");
        }
    }

    private static void displayProducts() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/products/readAllProducts"))
                    .header("user-email", authenticatedUserEmail)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                List<?> products = objectMapper.readValue(response.body(), List.class);
                System.out.println("=== Products ===");
                products.forEach(System.out::println);
            } else {
                System.out.println("Failed to fetch products. Error: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error during fetching products: " + e.getMessage());
        }
    }

    private static void fetchProductById(Scanner scanner) {
        System.out.print("Enter Product ID: ");
        String id = scanner.nextLine();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/products/readProductById/" + id))
                    .header("user-email", authenticatedUserEmail)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("Product: " + response.body());
            } else {
                System.out.println("Failed to fetch product. Error: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error during fetching product: " + e.getMessage());
        }
    }

    private static void addProduct(Scanner scanner) {
//        System.out.print("Enter Product ID: ");
//        String id = scanner.nextLine();
        System.out.print("Enter Product Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Product Price: ");
        double price = Double.parseDouble(scanner.nextLine());
        System.out.print("Enter Expiration Date (yyyy-MM-dd): ");
        String expirationDate = scanner.nextLine();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/products/create"))
                    .header("user-email", authenticatedUserEmail)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(Map.of(
                            "name", name,
                            "price", price,
                            "expirationDate", expirationDate
                    ))))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("Product added successfully!");
            } else {
                System.out.println(response.body());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error during adding product: " + e.getMessage());
        }
    }

    private static void updateProduct(Scanner scanner) {
        System.out.print("Enter Product ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter Product Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Product Price: ");
        double price = Double.parseDouble(scanner.nextLine());
        System.out.print("Enter Expiration Date (yyyy-MM-dd): ");
        String expirationDate = scanner.nextLine();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/products/updateProduct"))
                    .header("user-email", authenticatedUserEmail)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(Map.of(
                            "id", id,
                            "name", name,
                            "price", price,
                            "expirationDate", expirationDate
                    ))))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("Product updated successfully!");
            } else {
                System.out.println("Failed to update product. Error: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error during updating product: " + e.getMessage());
        }
    }

    private static void deleteProduct(Scanner scanner) {
        System.out.print("Enter Product ID: ");
        String id = scanner.nextLine();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/products/deleteProduct/" + id))
                    .header("user-email", authenticatedUserEmail)
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("Product deleted successfully!");
            } else {
                System.out.println("Failed to delete product. Error: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error during deleting product: " + e.getMessage());
        }
    }

    private static void viewTopExpensiveProducts() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/products/most-expensive-products"))
                    .header("user-email", authenticatedUserEmail)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                List<?> products = objectMapper.readValue(response.body(), List.class);
                System.out.println("=== Top 3 Expensive Products ===");
                products.forEach(System.out::println);
            } else {
                System.out.println("Failed to fetch top expensive products. Error: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error during fetching top expensive products: " + e.getMessage());
        }
    }
}
