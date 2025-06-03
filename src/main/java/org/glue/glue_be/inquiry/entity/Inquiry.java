package org.glue.glue_be.inquiry.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.glue.glue_be.common.BaseEntity;
import org.glue.glue_be.user.entity.User;


@Entity
@Getter
@Table(name = "inquiry")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inquiry extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column
	private Long inquiryId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String content;

	@Column(nullable = false)
	private Integer inquiryType;

	@Column(nullable = false)
	private String responseEmail;

	@Builder
	public Inquiry(User user, String title, String content, Integer inquiryType, String responseEmail) {
		this.user = user;
		this.title = title;
		this.content = content;
		this.inquiryType = inquiryType;
		this.responseEmail = responseEmail;
	}

}
