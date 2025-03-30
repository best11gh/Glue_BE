package org.glue.glue_be.user.entity;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name = "post_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "image_order", nullable = false)
    private Integer imageOrder;

    @Builder
    private PostImage(Post post, String imageUrl, Integer imageOrder) {
        this.post = post;
        this.imageUrl = imageUrl;
        this.imageOrder = imageOrder;
    }

    public static PostImage createPostImage(Post post, String imageUrl, Integer imageOrder) {
        return PostImage.builder()
                .post(post)
                .imageUrl(imageUrl)
                .imageOrder(imageOrder)
                .build();
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void updateImageOrder(Integer imageOrder) {
        this.imageOrder = imageOrder;
    }
}