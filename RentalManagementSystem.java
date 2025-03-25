import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

class House {
    private String id;
    private String location;
    private double price;
    private int bedrooms;
    private String owner;
    private boolean isAvailable;

    public House(String id, String location, double price, int bedrooms, String owner) {
        this.id = id;
        this.location = location;
        this.price = price;
        this.bedrooms = bedrooms;
        this.owner = owner;
        this.isAvailable = true;
    }

    public String getId() { return id; }
    public String getLocation() { return location; }
    public double getPrice() { return price; }
    public int getBedrooms() { return bedrooms; }
    public String getOwner() { return owner; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { this.isAvailable = available; }

    @Override
    public String toString() {
        return "[" + id + ", " + location + ", " + price + ", " + bedrooms + ", " + owner + "]";
    }
}

class Tenant {
    private String id;
    private String name;
    private String contact;
    private String preferredLocation;

    public Tenant(String id, String name, String contact, String preferredLocation) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.preferredLocation = preferredLocation;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getContact() { return contact; }
    public String getPreferredLocation() { return preferredLocation; }

    @Override
    public String toString() {
        return "[" + id + ", " + name + ", " + contact + ", " + preferredLocation + "]";
    }
}

class RentalAgreement {
    private String id;
    private House house;
    private Tenant tenant;
    private LocalDate startDate;
    private LocalDate endDate;
    private double deposit;

    public RentalAgreement(String id, House house, Tenant tenant, LocalDate startDate, LocalDate endDate, double deposit) {
        this.id = id;
        this.house = house;
        this.tenant = tenant;
        this.startDate = startDate;
        this.endDate = endDate;
        this.deposit = deposit;
    }

    public String getId() { return id; }
    public House getHouse() { return house; }
    public Tenant getTenant() { return tenant; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public double getDeposit() { return deposit; }

    @Override
    public String toString() {
        return "Agreement ID: " + id + "\n" +
               "House: " + house.toString() + "\n" +
               "Tenant: " + tenant.toString() + "\n" +
               "Start Date: " + startDate + "\n" +
               "End Date: " + endDate + "\n" +
               "Deposit: " + deposit;
    }
}

class HouseNotFoundException extends Exception {
    public HouseNotFoundException(String message) {
        super(message);
    }
}

public class RentalManagementSystem {
    private List<House> houses = new ArrayList<>();
    private List<Tenant> tenants = new ArrayList<>();
    private List<RentalAgreement> agreements = new ArrayList<>();
    private Map<String, House> houseMap = new HashMap<>();
    private Map<String, Tenant> tenantMap = new HashMap<>();
    private static int nextAgreementId = 1;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        RentalManagementSystem system = new RentalManagementSystem();
        system.loadData();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("1. Add House");
            System.out.println("2. Remove House");
            System.out.println("3. Search Houses");
            System.out.println("4. Register Tenant");
            System.out.println("5. Book House");
            System.out.println("6. Exit");
            System.out.print("Choose an option: ");
            int choice;
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
                continue;
            }
            switch (choice) {
                case 1: system.addHouse(scanner); break;
                case 2: system.removeHouse(scanner); break;
                case 3: system.searchHouses(scanner); break;
                case 4: system.registerTenant(scanner); break;
                case 5: system.bookHouse(scanner); break;
                case 6:
                    system.saveData();
                    System.out.println("Exiting...");
                    scanner.close();
                    return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private void loadData() {
        loadHouses();
        loadTenants();
        loadAgreements();
        for (RentalAgreement agreement : agreements) {
            agreement.getHouse().setAvailable(false);
        }
        int maxId = agreements.stream()
                .map(a -> Integer.parseInt(a.getId().substring(1)))
                .max(Integer::compare)
                .orElse(0);
        nextAgreementId = maxId + 1;
    }

    private void loadHouses() {
        try (BufferedReader reader = new BufferedReader(new FileReader("houses.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    String id = parts[0].trim();
                    String location = parts[1].trim();
                    double price = Double.parseDouble(parts[2].trim());
                    int bedrooms = Integer.parseInt(parts[3].trim());
                    String owner = parts[4].trim();
                    House house = new House(id, location, price, bedrooms, owner);
                    houses.add(house);
                    houseMap.put(id, house);
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist yet, start with empty list
        } catch (IOException e) {
            System.out.println("Error loading houses: " + e.getMessage());
        }
    }

    private void loadTenants() {
        try (BufferedReader reader = new BufferedReader(new FileReader("tenants.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String id = parts[0].trim();
                    String name = parts[1].trim();
                    String contact = parts[2].trim();
                    String preferredLocation = parts[3].trim();
                    Tenant tenant = new Tenant(id, name, contact, preferredLocation);
                    tenants.add(tenant);
                    tenantMap.put(id, tenant);
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist yet, start with empty list
        } catch (IOException e) {
            System.out.println("Error loading tenants: " + e.getMessage());
        }
    }

    private void loadAgreements() {
        try (BufferedReader reader = new BufferedReader(new FileReader("agreements.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    String id = parts[0].trim();
                    String houseId = parts[1].trim();
                    String tenantId = parts[2].trim();
                    LocalDate startDate = LocalDate.parse(parts[3].trim(), DATE_FORMATTER);
                    LocalDate endDate = LocalDate.parse(parts[4].trim(), DATE_FORMATTER);
                    double deposit = Double.parseDouble(parts[5].trim());
                    House house = houseMap.get(houseId);
                    Tenant tenant = tenantMap.get(tenantId);
                    if (house != null && tenant != null) {
                        RentalAgreement agreement = new RentalAgreement(id, house, tenant, startDate, endDate, deposit);
                        agreements.add(agreement);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist yet, start with empty list
        } catch (IOException e) {
            System.out.println("Error loading agreements: " + e.getMessage());
        }
    }

    private void saveData() {
        try {
            saveHouses();
            saveTenants();
            saveAgreements();
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    private void saveHouses() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("houses.txt"))) {
            for (House house : houses) {
                writer.write(String.format("%s,%s,%.2f,%d,%s",
                        house.getId(), house.getLocation(), house.getPrice(), house.getBedrooms(), house.getOwner()));
                writer.newLine();
            }
        }
    }

    private void saveTenants() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("tenants.txt"))) {
            for (Tenant tenant : tenants) {
                writer.write(String.format("%s,%s,%s,%s",
                        tenant.getId(), tenant.getName(), tenant.getContact(), tenant.getPreferredLocation()));
                writer.newLine();
            }
        }
    }

    private void saveAgreements() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("agreements.txt"))) {
            for (RentalAgreement agreement : agreements) {
                writer.write(String.format("%s,%s,%s,%s,%s,%.2f",
                        agreement.getId(), agreement.getHouse().getId(), agreement.getTenant().getId(),
                        agreement.getStartDate().format(DATE_FORMATTER), agreement.getEndDate().format(DATE_FORMATTER),
                        agreement.getDeposit()));
                writer.newLine();
            }
        }
    }

    private void addHouse(Scanner scanner) {
        System.out.print("Enter House ID: ");
        String id = scanner.nextLine();
        if (houseMap.containsKey(id)) {
            System.out.println("House ID already exists.");
            return;
        }
        System.out.print("Enter Location: ");
        String location = scanner.nextLine();
        System.out.print("Enter Price: ");
        double price;
        try {
            price = scanner.nextDouble();
            if (price < 0) throw new IllegalArgumentException("Price cannot be negative.");
        } catch (Exception e) {
            System.out.println("Invalid price: " + e.getMessage());
            scanner.nextLine();
            return;
        }
        System.out.print("Enter Bedrooms: ");
        int bedrooms;
        try {
            bedrooms = scanner.nextInt();
            if (bedrooms < 0) throw new IllegalArgumentException("Bedrooms cannot be negative.");
        } catch (Exception e) {
            System.out.println("Invalid bedrooms: " + e.getMessage());
            scanner.nextLine();
            return;
        }
        scanner.nextLine(); // Consume newline
        System.out.print("Enter Owner: ");
        String owner = scanner.nextLine();
        House house = new House(id, location, price, bedrooms, owner);
        houses.add(house);
        houseMap.put(id, house);
        try {
            saveHouses();
            System.out.println("House added successfully.");
        } catch (IOException e) {
            System.out.println("Error saving houses: " + e.getMessage());
        }
    }

    private void removeHouse(Scanner scanner) {
        System.out.print("Enter House ID to remove: ");
        String id = scanner.nextLine();
        House house = houseMap.get(id);
        if (house == null) {
            System.out.println("House not found.");
            return;
        }
        houses.remove(house);
        houseMap.remove(id);
        try {
            saveHouses();
            System.out.println("House removed successfully.");
        } catch (IOException e) {
            System.out.println("Error saving houses: " + e.getMessage());
        }
    }

    private void searchHouses(Scanner scanner) {
        System.out.print("Enter Location: ");
        String location = scanner.nextLine();
        System.out.print("Enter Max Price: ");
        double maxPrice;
        try {
            maxPrice = scanner.nextDouble();
            if (maxPrice < 0) throw new IllegalArgumentException("Max price cannot be negative.");
        } catch (Exception e) {
            System.out.println("Invalid max price: " + e.getMessage());
            scanner.nextLine();
            return;
        }
        scanner.nextLine(); // Consume newline
        List<House> result = houses.stream()
                .filter(h -> h.isAvailable())
                .filter(h -> h.getLocation().equalsIgnoreCase(location))
                .filter(h -> h.getPrice() <= maxPrice)
                .collect(Collectors.toList());
        System.out.println("Found " + result.size() + " house(s):");
        for (House house : result) {
            System.out.println(house);
        }
    }

    private void registerTenant(Scanner scanner) {
        System.out.print("Enter Tenant ID: ");
        String id = scanner.nextLine();
        if (tenantMap.containsKey(id)) {
            System.out.println("Tenant ID already exists.");
            return;
        }
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Contact: ");
        String contact = scanner.nextLine();
        System.out.print("Enter Preferred Location: ");
        String preferredLocation = scanner.nextLine();
        Tenant tenant = new Tenant(id, name, contact, preferredLocation);
        tenants.add(tenant);
        tenantMap.put(id, tenant);
        try {
            saveTenants();
            System.out.println("Tenant registered successfully.");
            // Suggest matching houses
            List<House> matches = houses.stream()
                    .filter(h -> h.isAvailable())
                    .filter(h -> h.getLocation().equalsIgnoreCase(preferredLocation))
                    .collect(Collectors.toList());
            if (!matches.isEmpty()) {
                System.out.println("Available houses in " + preferredLocation + ":");
                matches.forEach(System.out::println);
            }
        } catch (IOException e) {
            System.out.println("Error saving tenants: " + e.getMessage());
        }
    }

    private synchronized void bookHouse(Scanner scanner) {
        System.out.print("Enter House ID: ");
        String houseId = scanner.nextLine();
        House house = houseMap.get(houseId);
        if (house == null) {
            System.out.println("House not found.");
            return;
        }
        if (!house.isAvailable()) {
            System.out.println("House is not available.");
            return;
        }
        System.out.print("Enter Tenant ID: ");
        String tenantId = scanner.nextLine();
        Tenant tenant = tenantMap.get(tenantId);
        if (tenant == null) {
            System.out.println("Tenant not found.");
            return;
        }
        System.out.print("Enter Start Date (yyyy-MM-dd): ");
        LocalDate startDate;
        try {
            startDate = LocalDate.parse(scanner.nextLine(), DATE_FORMATTER);
        } catch (Exception e) {
            System.out.println("Invalid start date format.");
            return;
        }
        System.out.print("Enter End Date (yyyy-MM-dd): ");
        LocalDate endDate;
        try {
            endDate = LocalDate.parse(scanner.nextLine(), DATE_FORMATTER);
            if (endDate.isBefore(startDate)) throw new IllegalArgumentException("End date must be after start date.");
        } catch (Exception e) {
            System.out.println("Invalid end date: " + e.getMessage());
            return;
        }
        System.out.print("Enter Deposit: ");
        double deposit;
        try {
            deposit = scanner.nextDouble();
            if (deposit < 0) throw new IllegalArgumentException("Deposit cannot be negative.");
        } catch (Exception e) {
            System.out.println("Invalid deposit: " + e.getMessage());
            scanner.nextLine();
            return;
        }
        scanner.nextLine(); // Consume newline
        String agreementId = "A" + nextAgreementId++;
        RentalAgreement agreement = new RentalAgreement(agreementId, house, tenant, startDate, endDate, deposit);
        agreements.add(agreement);
        house.setAvailable(false);
        try {
            saveAgreements();
            System.out.println("House booked successfully.");
            System.out.println(agreement);
        } catch (IOException e) {
            System.out.println("Error saving agreements: " + e.getMessage());
        }
    }
}