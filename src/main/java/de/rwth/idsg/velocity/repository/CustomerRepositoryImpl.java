package de.rwth.idsg.velocity.repository;

import de.rwth.idsg.velocity.domain.Address;
import de.rwth.idsg.velocity.domain.Customer;
import de.rwth.idsg.velocity.web.rest.dto.modify.CreateEditCustomerDTO;
import de.rwth.idsg.velocity.web.rest.dto.view.ViewCustomerDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.List;

/**
 * Created by sgokay on 05.06.14.
 */
@Repository
@Transactional
public class CustomerRepositoryImpl implements CustomerRepository {

    private static final Logger log = LoggerFactory.getLogger(CustomerRepositoryImpl.class);

    private enum Operation { CREATE, UPDATE };
    private enum FindType { ALL, BY_NAME, BY_LOGIN };

    @PersistenceContext
    EntityManager em;

    @Override
    public List<ViewCustomerDTO> findAll() {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        return em.createQuery(
                getQuery(builder, FindType.ALL, null, null, null)
        ).getResultList();
    }

    @Override
    public List<ViewCustomerDTO> findbyName(String firstname, String lastname) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        return em.createQuery(
                getQuery(builder, FindType.BY_NAME, firstname, lastname, null)
        ).getResultList();
    }

    @Override
    public ViewCustomerDTO findbyLogin(String login) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        return em.createQuery(
                getQuery(builder, FindType.BY_LOGIN, null, null, login)
        ).getSingleResult();
    }

    @Override
    public void create(CreateEditCustomerDTO dto) {
        Customer customer = new Customer();
        setFields(customer, dto, Operation.CREATE);
        em.persist(customer);
        log.debug("Created new customer {}", customer);
    }

    @Override
    public void update(CreateEditCustomerDTO dto) {
        final String login = dto.getLogin();
        if (login == null) {
            return;
        }

        Customer customer = em.find(Customer.class, login);
        if (customer == null) {
            log.error("No customer with login: {} to update.", login);
        } else {
            setFields(customer, dto, Operation.UPDATE);
            em.merge(customer);
            log.debug("Updated customer {}", customer);
        }
    }

    @Override
    public void delete(String login) {
        Customer customer = em.find(Customer.class, login);
        if (customer == null) {
            log.error("No customer with login: {} to delete.", login);
        } else {
            em.remove(customer);
            log.debug("Deleted customer {}", customer);
        }
    }

    /**
     * This method sets the fields of the customer to the values in DTO.
     *
     */
    private void setFields(Customer customer, CreateEditCustomerDTO dto, Operation operation) {

        // TODO: Should the login be changeable? Who sets the field? Clarify!
        customer.setLogin(dto.getLogin());

        customer.setCustomerId(dto.getCustomerId());
        customer.setCardId(dto.getCardId());
        customer.setFirstname(dto.getFirstname());
        customer.setLastname(dto.getLastname());
        customer.setBirthday(dto.getBirthday());
        customer.setIsActivated(dto.getIsActivated());

        switch (operation) {
            case CREATE:
                // for create (brand new address entity)
                customer.setAddress(dto.getAddress());
                break;

            case UPDATE:
                // for edit (keep the address ID)
                Address add = customer.getAddress();
                Address dtoAdd = dto.getAddress();
                add.setStreetAndHousenumber(dtoAdd.getStreetAndHousenumber());
                add.setZip(dtoAdd.getZip());
                add.setCity(dtoAdd.getCity());
                add.setCountry(dtoAdd.getCountry());
                break;
        }
    }

    /**
     * This method returns the query to get information of customers for various lookup cases
     *
     */
    private CriteriaQuery<ViewCustomerDTO> getQuery(CriteriaBuilder builder, FindType findType,
                                                        String firstname, String lastname,
                                                        String login) {
        CriteriaQuery<ViewCustomerDTO> criteria = builder.createQuery(ViewCustomerDTO.class);
        Root<Customer> root = criteria.from(Customer.class);

        criteria.select(
                builder.construct(
                        ViewCustomerDTO.class,
                        root.get("login"),
                        root.get("customerId"),
                        root.get("firstname"),
                        root.get("lastname"),
                        root.get("isActivated"),
                        root.get("birthday"),
                        root.get("cardId")
                )
        );

        switch (findType) {
            case ALL:
                break;

            // Case insensitive search
            case BY_NAME:
                Path<String> firstPath = root.get("firstname");
                Expression<String> firstLower = builder.lower(firstPath);

                Path<String> lastPath = root.get("lastname");
                Expression<String> lastLower = builder.lower(lastPath);

                criteria.where(
                        builder.and(
                                builder.equal(firstLower, firstname.toLowerCase()),
                                builder.equal(lastLower, lastname.toLowerCase())
                        )
                );
                break;

            // Case insensitive search
            case BY_LOGIN:
                Path<String> loginPath = root.get("login");
                Expression<String> loginLower = builder.lower(loginPath);

                criteria.where(
                        builder.equal(loginLower, login.toLowerCase())
                );
                break;
        }

        return criteria;
    }
}