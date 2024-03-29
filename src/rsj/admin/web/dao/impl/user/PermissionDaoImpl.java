package rsj.admin.web.dao.impl.user;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import rsj.admin.web.dao.user.PermissionDao;
import rsj.admin.web.domain.user.Permission;

public class PermissionDaoImpl extends HibernateDaoSupport implements PermissionDao {

	@Override
	public void merge(Permission permission) {
		// TODO Auto-generated method stub
		getHibernateTemplate().merge(permission);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Permission> list(final Permission permission) {
		// TODO Auto-generated method stub
		return (List<Permission>) getHibernateTemplate().execute(
				new HibernateCallback() {
					public Object doInHibernate(Session session)
							throws HibernateException {
						StringBuffer hql = new StringBuffer("from Permission p where 1 = 1");
						if(permission != null && permission.getMenuID() != null){
							hql.append(" and p.menuID = :menuID");
						}
						hql.append(" order by p.orderView desc,p.id");
						Query query = session.createQuery(hql.toString());
								
						if(permission != null && permission.getMenuID() != null){
							query.setParameter("menuID", permission.getMenuID());
						}
						return query.list();
					}
				});
	}

	@Override
	public Permission get(Long ID) {
		// TODO Auto-generated method stub
		return getHibernateTemplate().get(Permission.class, ID);
	}

	@Override
	public void del(Permission permission) {
		// TODO Auto-generated method stub
		getHibernateTemplate().delete(permission);
	}

}
