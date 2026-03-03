package iscae.mr.app_donation.ai.services;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiAgentService {

    private final ChatClient chatClient;

    public AiAgentService(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("Tu es un assistant intelligent pour une plateforme de donation nommée 'Khayriati'. " +
                        "Tu peux aider les utilisateurs à trouver des projets, des organisations et des activités. " +
                        "Tu as également accès aux informations personnelles de l'utilisateur connecté (ses dons). " +
                        "Utilise les outils à ta disposition pour obtenir des informations réelles de la base de données. "
                        +
                        "Si un utilisateur demande ses dons, utilise 'getMesDons'. " +
                        "Si un utilisateur demande la progression d'un projet, utilise 'getProjetDetail' avec l'ID du projet. "
                        +
                        "Réponds de manière polie, chaleureuse et concise en français. " +
                        "IMPORTANT: Organise tes réponses en utilisant le format Markdown (listes à puces, gras, titres si nécessaire) pour que ce soit très lisible. "
                        +
                        "Si tu ne trouves pas d'information, suggère à l'utilisateur de contacter le support.")
                .defaultFunctions("getProjetsActifs", "getOrganisations", "getActivites", "getMesDons",
                        "getProjetDetail")
                .build();
    }

    public String chat(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}
