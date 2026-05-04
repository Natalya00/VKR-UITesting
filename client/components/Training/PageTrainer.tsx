import React, { useState, useEffect } from 'react';
import './Training.css';
import { generateId, generateName, generateClass, generateDataId } from '../../utils/attributeGenerator';

interface PageElement {
  tag: string;
  id?: string;
  className?: string;
  href?: string;
  text?: string;
  type?: string;
  'data-id'?: string;
  'data-role'?: string;
  src?: string;
  srcDoc?: string;
  value?: string;
  placeholder?: string;
  inputName?: string;
  style?: string;
  checked?: boolean;
  selected?: boolean;
  multiple?: boolean;
  disabled?: boolean;
  children?: PageElement[];
  [key: string]: string | PageElement[] | boolean | undefined;
}

interface PageTrainerConfig {
  elements: PageElement[];
  targetSelector: string;
  pageStyle?: {
    backgroundColor?: string;
  };
  pageTitle?: string;
  layout?: 'default' | 'cards' | 'table' | 'navigation';
  disableDynamicAttrs?: boolean;
  onLoadScript?: string;
  exerciseId?: string;
}

interface PageTrainerProps {
  config: PageTrainerConfig;
}

const PageTrainer: React.FC<PageTrainerProps> = ({ config }) => {
  const { elements, pageStyle, pageTitle = '', layout = 'default', disableDynamicAttrs = false, onLoadScript } = config;

  useEffect(() => {
    if (onLoadScript) {
      try {
        const script = new Function(onLoadScript);
        script();
      } catch (e) {
      }
    }

    return () => {
      const hasIframe = elements.some(e => e.tag === 'iframe');
      if (!hasIframe) {
        const container = document.querySelector('.page-trainer .page-content');
        if (container) {
          const clone = container.cloneNode(true);
          container.parentNode?.replaceChild(clone, container);
        }
      }
    };
  }, [onLoadScript]);

  const [generatedAttrs] = useState(() => {
    if (disableDynamicAttrs) {
      return {};
    }

    const attrs: Record<string, any> = {};

    const processElement = (el: PageElement, path: string) => {
      if (el.id) {
        const prefixMatch = el.id.match(/^([a-z]+_)/);
        const suffixMatch = el.id.match(/(_[a-z]+)$/);
        attrs[`${path}_id`] = generateId(
          8,
          suffixMatch ? suffixMatch[1] : undefined,
          prefixMatch ? prefixMatch[1] : undefined
        );
      }
      if (el.className) attrs[`${path}_class`] = generateClass();
      if (el['data-id']) attrs[`${path}_data-id`] = generateDataId();
      if (el['data-role']) attrs[`${path}_data-role`] = el['data-role'];
      if (el.inputName) attrs[`${path}_name`] = generateName();

      if (el.children) {
        el.children.forEach((child: PageElement, idx: number) => {
          processElement(child, `${path}_child${idx}`);
        });
      }
    };

    config.elements.forEach((el, idx) => {
      processElement(el, `el${idx}`);
    });

    return attrs;
  });

  const renderElement = (element: PageElement, index: number | string, path: string) => {
    const { tag, text, className, href, type, src, srcDoc, value, children, placeholder: inputPlaceholder, name: inputName, style: styleString, checked: inputChecked, id: elementId, attrs: elementAttrs, ...props } = element as any;

    let generatedClassName = className;
    if (className && generatedAttrs[`${path}_class`]) {
      generatedClassName = `${className} ${generatedAttrs[`${path}_class`]}`;
    }
    const generatedId = elementId && generatedAttrs[`${path}_id`] ? generatedAttrs[`${path}_id`] : elementId;

    const validProps: Record<string, any> = {};

    if (elementId) {
      validProps.id = generatedId || elementId;
    }

    if (elementAttrs && typeof elementAttrs === 'object') {
      Object.entries(elementAttrs).forEach(([key, val]) => {
        if (val !== undefined) validProps[key] = val;
      });
    }

    Object.entries(props).forEach(([key, val]) => {
      if (val !== undefined && key !== 'tag' && key !== 'text' && key !== 'children' && key !== 'attrs') {
        if (key === 'data-id' && generatedAttrs[`${path}_data-id`]) {
          validProps[key] = generatedAttrs[`${path}_data-id`];
        } else if (key === 'data-role' && generatedAttrs[`${path}_data-role`]) {
          validProps[key] = generatedAttrs[`${path}_data-role`];
        } else {
          validProps[key] = val as string;
        }
      }
    });

    const style: React.CSSProperties = {};
    if (styleString) {
      styleString.split(';').forEach((rule: string) => {
        const parts = rule.split(':');
        if (parts.length >= 2) {
          const property = parts[0].trim();
          const value = parts.slice(1).join(':').trim();
          if (property && value) {
            const camelCaseProp = property.replace(/-([a-z])/g, (g: string) => g[1].toUpperCase());
            (style as any)[camelCaseProp] = value;
          }
        }
      });
    }

    const renderChildren = () => {
      if (!children || children.length === 0) return text;
      return children.map((child: PageElement, childIndex: number) =>
        renderElement(child, `${index}-${childIndex}`, `${path}_child${childIndex}`)
      );
    };

    switch (tag) {
      case 'a':
        return (
          <a key={index} href={href} className={generatedClassName} {...validProps}>
            {text}
          </a>
        );
      case 'button':
        return (
          <button key={index} className={generatedClassName} type={type || 'button'} {...validProps}>
            {children && children.length > 0 ? renderChildren() : text}
          </button>
        );
      case 'div':
        return (
          <div key={index} className={generatedClassName} style={Object.keys(style).length > 0 ? style : undefined} {...validProps}>
            {renderChildren()}
          </div>
        );
      case 'span':
        return (
          <span key={index} className={generatedClassName} style={Object.keys(style).length > 0 ? style : undefined} {...validProps}>
            {text}
          </span>
        );
      case 'p':
        return (
          <p key={index} className={generatedClassName} style={Object.keys(style).length > 0 ? style : undefined} {...validProps}>
            {text}
          </p>
        );
      case 'h1':
      case 'h2':
      case 'h3':
      case 'h4':
        const HeadingTag = tag as 'h1' | 'h2' | 'h3' | 'h4';
        return (
          <HeadingTag key={index} className={generatedClassName} {...validProps}>
            {text}
          </HeadingTag>
        );
      case 'img':
        return (
          <img key={index} src={src || '/placeholder.png'} alt={text} className={generatedClassName} {...validProps} />
        );
      case 'iframe':
        const iframeSrc = srcDoc ? `data:text/html;charset=utf-8;base64,${btoa(unescape(encodeURIComponent(srcDoc)))}` : src;
        return (
          <iframe
            key={index}
            src={iframeSrc}
            title={text || elementId || element.name || 'iframe'}
            className={generatedClassName}
            style={Object.keys(style).length > 0 ? style : undefined}
            {...validProps}
          />
        );
      case 'table':
        return (
          <table key={index} className={generatedClassName} {...validProps}>
            {renderChildren()}
          </table>
        );
      case 'tr':
        return (
          <tr key={index} className={generatedClassName} {...validProps}>
            {renderChildren()}
          </tr>
        );
      case 'td':
        return (
          <td key={index} className={generatedClassName} {...validProps}>
            {text}
          </td>
        );
      case 'th':
        return (
          <th key={index} className={generatedClassName} {...validProps}>
            {text}
          </th>
        );
      case 'thead':
        return (
          <thead key={index} className={generatedClassName} {...validProps}>
            {renderChildren()}
          </thead>
        );
      case 'tbody':
        return (
          <tbody key={index} className={generatedClassName} {...validProps}>
            {renderChildren()}
          </tbody>
        );
      case 'ul':
        return (
          <ul key={index} className={generatedClassName} {...validProps}>
            {renderChildren()}
          </ul>
        );
      case 'li':
        return (
          <li key={index} className={generatedClassName} {...validProps}>
            {text}
          </li>
        );
      case 'input':
        const isChecked = type === 'checkbox' || type === 'radio' ? !!inputChecked : false;
        const generatedInputName = inputName && generatedAttrs[`${path}_name`] ? generatedAttrs[`${path}_name`] : inputName;
        const isDisabled = element.disabled === true;
        return (
          <input
            key={index}
            type={type || 'text'}
            className={generatedClassName}
            placeholder={inputPlaceholder}
            name={generatedInputName}
            defaultValue={value}
            defaultChecked={isChecked}
            disabled={isDisabled}
            {...(isChecked ? { 'data-checked': 'true' } : {})}
            style={Object.keys(style).length > 0 ? style : undefined}
            {...validProps}
          />
        );
      case 'textarea':
        return (
          <textarea
            key={index}
            className={generatedClassName}
            placeholder={inputPlaceholder}
            defaultValue={value}
            style={Object.keys(style).length > 0 ? style : undefined}
            {...validProps}
          >
            {text}
          </textarea>
        );
      case 'label':
        return (
          <label key={index} className={generatedClassName} {...validProps}>
            {children && children.length > 0 ? renderChildren() : text}
          </label>
        );
      case 'select':
        const isMultiple = element.multiple === true;
        return (
          <select key={index} className={generatedClassName} multiple={isMultiple} {...validProps}>
            {renderChildren()}
          </select>
        );
      case 'option':
        const isSelected = element.selected === true;
        return (
          <option key={index} value={element.value} selected={isSelected} {...validProps}>
            {text}
          </option>
        );
      case 'form':
        return (
          <form key={index} className={generatedClassName} style={Object.keys(style).length > 0 ? style : undefined} {...validProps}>
            {renderChildren()}
          </form>
        );
      case 'header':
        return (
          <header key={index} className={generatedClassName} style={Object.keys(style).length > 0 ? style : undefined} {...validProps}>
            {renderChildren()}
          </header>
        );
      case 'main':
        return (
          <main key={index} className={generatedClassName} style={Object.keys(style).length > 0 ? style : undefined} {...validProps}>
            {renderChildren()}
          </main>
        );
      case 'footer':
        return (
          <footer key={index} className={generatedClassName} style={Object.keys(style).length > 0 ? style : undefined} {...validProps}>
            {renderChildren()}
          </footer>
        );
      case 'article':
        return (
          <article key={index} className={generatedClassName} style={Object.keys(style).length > 0 ? style : undefined} {...validProps}>
            {renderChildren()}
          </article>
        );
      case 'time':
        return (
          <time key={index} className={generatedClassName} {...validProps}>
            {text}
          </time>
        );
      case 'svg':
        return (
          <svg key={index} className={generatedClassName} {...validProps}>
            {renderChildren()}
          </svg>
        );
      case 'path':
        return <path key={index} {...validProps} />;
      case 'circle':
        return <circle key={index} {...validProps} />;
      case 'polyline':
        return <polyline key={index} {...validProps} />;
      case 'rect':
        return <rect key={index} {...validProps} />;
      case 'line':
        return <line key={index} {...validProps} />;
      case 'title':
        return <title key={index}>{text}</title>;
      default:
        const Component = tag as React.ElementType;
        return (
          <Component key={index} className={generatedClassName} {...validProps}>
            {renderChildren()}
          </Component>
        );
    }
  };

  const renderLayout = () => {
    switch (layout) {
      case 'cards':
        return (
          <div className="cards-grid">
            {elements.map((element, index) => (
              <div key={index} className="card-item">
                {renderElement(element, index, `el${index}`)}
              </div>
            ))}
          </div>
        );
      case 'navigation':
        return (
          <div className="navigation-page">
            <nav className="nav-menu">
              {elements.map((element, index) => (
                <div key={index} className="nav-item">
                  {renderElement(element, index, `el${index}`)}
                </div>
              ))}
            </nav>
          </div>
        );
      case 'table':
        return (
          <div className="table-container">
            <table className="data-table">
              {elements.map((element, index) => renderElement(element, index, `el${index}`))}
            </table>
          </div>
        );
      default:
        return (
          <div className="elements-wrapper">
            {elements.map((element, index) => (
              <div key={index} className="element-item">
                {renderElement(element, index, `el${index}`)}
              </div>
            ))}
          </div>
        );
    }
  };

  return (
    <div
      key={config.exerciseId || (elements.some(e => e.tag === 'iframe') ? 'iframe-exercise' : `exercise-${pageTitle}-${elements.length}`)}
      className="training-container page-trainer"
      style={pageStyle}
    >
      <div className="page-content">
        {pageTitle && <h2 className="page-title">{pageTitle}</h2>}
        {renderLayout()}
      </div>
    </div>
  );
};

export default PageTrainer;