import React, { useState } from 'react';
import './NavigationTrainer.css';
import { generateId, generateClass } from '../../utils/attributeGenerator';

interface NavItem {
  text: string;
  href: string;
  id?: string;
  className?: string;
  dataPage?: string;
  dataRole?: string;
  hasSubmenu?: boolean;
  submenu?: NavItem[];
  children?: NavItem[];
  dataTarget?: string | boolean;
  'data-target'?: string | boolean;
}

interface NavigationTrainerConfig {
  pageTitle: string;
  menuId?: string;
  items: NavItem[];
  targetSelector: string;
  layout?: 'horizontal' | 'vertical' | 'sidebar';
  disableDynamicAttrs?: boolean;
  exerciseId?: string;
}

interface NavigationTrainerProps {
  config: NavigationTrainerConfig;
}

const NavigationTrainer: React.FC<NavigationTrainerProps> = ({ config }) => {
  const { pageTitle, menuId, items, layout = 'default', disableDynamicAttrs = false } = config;

  const effectiveLayout = (layout as string) === 'navigation' ? 'horizontal' :
                          (layout as string) === 'cards' ? 'vertical' :
                          (layout as string) || 'horizontal';

  const [generatedAttrs] = useState(() => {
    if (disableDynamicAttrs) {
      return {};
    }

    const attrs: Record<string, any> = {};

    const processItem = (item: NavItem, path: string) => {
      if (item.id) {
        attrs[`${path}_id`] = generateId(8, undefined, 'nav_');
      }
      if (item.className) {
        attrs[`${path}_class`] = generateClass();
      }
      const childItems = item.submenu || item.children;
      if (childItems) {
        childItems.forEach((sub, idx) => {
          processItem(sub, `${path}_sub${idx}`);
        });
      }
    };

    items.forEach((item, idx) => {
      processItem(item, `item${idx}`);
    });

    return attrs;
  });

  const renderNavItems = (navItems: NavItem[], level: number = 0): JSX.Element => {
    return (
      <ul className={level === 0 ? `nav-level-1 nav-${layout}` : `nav-level-${level + 1} submenu`}>
        {navItems.map((item, index) => {
          const path = `item${index}`;
          const generatedId = item.id && generatedAttrs[`${path}_id`] ? generatedAttrs[`${path}_id`] : item.id;
          const generatedClass = item.className && generatedAttrs[`${path}_class`]
            ? `${item.className} ${generatedAttrs[`${path}_class`]}`
            : item.className;

          const childItems = item.submenu || item.children;
          const hasChildren = item.hasSubmenu || (childItems && childItems.length > 0);

          return (
            <li key={index}>
              <a
                href={item.href}
                id={generatedId}
                data-page={item.dataPage}
                data-role={item.dataRole}
                data-target={(item['data-target'] || item.dataTarget) ? 'true' : undefined}
                className={`${generatedClass || ''} ${hasChildren ? 'has-submenu' : ''}`.trim()}
              >
                {item.text}
              </a>
              {hasChildren && childItems && (
                <div className="submenu-container">
                  <ul className="submenu">
                    {childItems.map((subItem, subIndex) => {
                      const subPath = `${path}_sub${subIndex}`;
                      const subGeneratedId = subItem.id && generatedAttrs[`${subPath}_id`]
                        ? generatedAttrs[`${subPath}_id`]
                        : subItem.id;

                      return (
                        <li key={subIndex}>
                          <a
                            href={subItem.href}
                            id={subGeneratedId}
                            data-target={(subItem['data-target'] || subItem.dataTarget) ? 'true' : undefined}
                          >
                            {subItem.text}
                          </a>
                        </li>
                      );
                    })}
                  </ul>
                </div>
              )}
            </li>
          );
        })}
      </ul>
    );
  };

  return (
    <div
      key={config.exerciseId}
      className="training-container navigation-trainer"
    >
      <h2 className="page-title">{pageTitle}</h2>
      <nav id={menuId || 'main-nav'} className={`main-navigation nav-${effectiveLayout}`}>
        {renderNavItems(items)}
      </nav>
    </div>
  );
};

export default NavigationTrainer;
