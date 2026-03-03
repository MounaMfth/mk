// src/main/java/iscae/mr/app_donation/utulisateur/dtos/RegisterRequest.java
package iscae.mr.app_donation.utulisateur.dtos;

public class RegisterRequest {
    private String username;
    private String password;
    private String telephone;
    private String role;
    private String name;
    private String location;

    // Getters/Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public UtilisateurDTO toUtilisateurDTO() {
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setUsername(this.username);
        dto.setPassword(this.password);
        dto.setEmail(this.username); // email = username
        dto.setTelephone(this.telephone);
        dto.setAdresse(this.location); // location -> adresse
        dto.setProfil((this.role == null || this.role.isBlank()) ? "ORGANISATEUR" : this.role.trim());
        // Si disponible :
        // dto.setNom(this.name);
        return dto;
    }
}
