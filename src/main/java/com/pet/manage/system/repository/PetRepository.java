package com.pet.manage.system.repository;

import com.pet.manage.system.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Long> {

	@Query("""
			select distinct p
			from Pet p
			left join fetch p.owner o
			left join fetch p.assignedVet av
			where exists (
				select 1 from PetMedicalChatMessage m where m.pet = p
			)
			and (
				:query is null or trim(:query) = ''
				or lower(p.petName) like lower(concat('%', :query, '%'))
				or lower(p.petType) like lower(concat('%', :query, '%'))
				or lower(p.breed) like lower(concat('%', :query, '%'))
				or lower(o.ownerName) like lower(concat('%', :query, '%'))
				or lower(o.phoneNumber) like lower(concat('%', :query, '%'))
				or str(p.id) like concat('%', :query, '%')
			)
			order by p.id desc
			""")
	List<Pet> searchPetsWithMedicalChat(@Param("query") String query);

	@Query("""
			select distinct p
			from Pet p
			left join fetch p.owner o
			left join fetch p.assignedVet av
			where (
				:query is null or trim(:query) = ''
				or lower(p.petName) like lower(concat('%', :query, '%'))
				or lower(p.petType) like lower(concat('%', :query, '%'))
				or lower(p.breed) like lower(concat('%', :query, '%'))
				or lower(o.ownerName) like lower(concat('%', :query, '%'))
				or lower(o.phoneNumber) like lower(concat('%', :query, '%'))
				or lower(o.email) like lower(concat('%', :query, '%'))
				or str(p.id) like concat('%', :query, '%')
			)
			order by p.id desc
			""")
	List<Pet> searchPetsForDoctor(@Param("query") String query);
}
