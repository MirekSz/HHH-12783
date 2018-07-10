
package org.hibernate.bugs;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.Before;
import org.junit.Test;

/**
 * This template demonstrates how to develop a standalone test case for Hibernate ORM. Although this is perfectly acceptable as a
 * reproducer, usage of ORMUnitTestCase is preferred!
 */
public class ORMStandaloneTestCase {

	private SessionFactory sf;

	@Before
	public void setup() {
		StandardServiceRegistryBuilder srb =
				new StandardServiceRegistryBuilder().applySetting("hibernate.show_sql", "true").applySetting("hibernate.format_sql", "true")
						.applySetting("hibernate.hbm2ddl.auto", "update").applySetting("hibernate.order_inserts", "true");

		Metadata metadata = new MetadataSources(srb.build()).addAnnotatedClass(Person.class).addAnnotatedClass(Team.class).buildMetadata();

		sf = metadata.buildSessionFactory();
	}

	@Test
	public void hhh12783Test() throws Exception {
		Session session = sf.openSession();

		Transaction beginTransaction = session.beginTransaction();

		Team firstTeam = new Team();
		firstTeam.setName("firstTeam");
		session.save(firstTeam);

		Person teamMember = new Person();
		teamMember.setName("mike");
		teamMember.setTeam(firstTeam);
		session.save(teamMember);

		Person coach = new Person();
		coach.setName("coach");
		coach.setTeam(firstTeam);
		session.save(coach);

		Team childTeam = new Team();
		childTeam.setName("secondTeam");
		childTeam.setCoach(coach); // coach plays as team member infirstTeam but is coach in childTeam
		session.save(childTeam);

		beginTransaction.commit(); // hibernate.order_inserts=false and works
	}

	@Entity
	public static class Person {

		@Id
		@GeneratedValue
		private Long id;

		@Column(unique = true)
		private String name;

		@JoinColumn(name = "ID_TEAM")
		@ManyToOne(fetch = FetchType.LAZY)
		private Team team;

		public Long getId() {
			return id;
		}

		public void setId(final Long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public Team getTeam() {
			return team;
		}

		public void setTeam(final Team team) {
			this.team = team;
		}
	}

	@Entity
	public static class Team {

		@Id
		@GeneratedValue
		private Long id;

		@Column(unique = true)
		private String name;
		@OneToMany(fetch = FetchType.LAZY, mappedBy = "team")
		private Set<Person> members = new HashSet<>();

		@JoinColumn(name = "ID_COACH", nullable = true)
		@ManyToOne(fetch = FetchType.LAZY, optional = true) // optionl/nullable is important in this case
		private Person coach; // one way relation

		public Long getId() {
			return id;
		}

		public void setId(final Long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public Person getCoach() {
			return coach;
		}

		public void setCoach(final Person coach) {
			this.coach = coach;
		}

		public Set<Person> getMembers() {
			return members;
		}

		public void setMembers(final Set<Person> members) {
			this.members = members;
		}

	}
}
