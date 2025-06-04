package org.glue.glue_be.inquiry.repository;


import org.glue.glue_be.inquiry.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;


public interface InquiryRepository extends JpaRepository<Inquiry, Long> {


}
