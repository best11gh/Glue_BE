package org.glue.glue_be.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "join_route")
public class JoinRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "join_route_id")
    private Long joinRouteId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotNull
    @Column(name = "join_route_type", nullable = false)
    private Integer joinRoute;


    protected JoinRoute() {
    }

    public JoinRoute(User user, Integer joinRoute) {
        this.user = user;
        this.joinRoute = joinRoute;
    }
}
