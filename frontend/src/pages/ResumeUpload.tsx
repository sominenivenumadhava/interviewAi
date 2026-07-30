import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Upload, FileText, CheckCircle2, X, CloudUpload } from 'lucide-react';
import {
  Card, CardContent, CardHeader, CardTitle, CardDescription, CardFooter,
} from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { ProgressBar } from '../components/ui/ProgressBar';
import { PageHeader } from '../components/ui/PageHeader';
import { mockResume } from '../data/mockData';

export function ResumeUpload() {
  const navigate = useNavigate();
  const [isDragging, setIsDragging]   = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [uploaded, setUploaded]       = useState(true); // Default true for demo

  const handleDragOver  = (e: React.DragEvent) => { e.preventDefault(); setIsDragging(true);  };
  const handleDragLeave = ()                    => { setIsDragging(false); };
  const handleDrop      = (e: React.DragEvent) => { e.preventDefault(); setIsDragging(false); simulateUpload(); };

  const simulateUpload = () => {
    setIsUploading(true);
    setTimeout(() => { setIsUploading(false); setUploaded(true); }, 1500);
  };

  return (
    <div className="mx-auto max-w-4xl space-y-8">
      <PageHeader
        title="Resume Context"
        subtitle="Upload your latest resume. Our AI will analyze it to personalize your interview questions."
      />

      <AnimatePresence mode="wait">
        {!uploaded ? (
          /* ── Upload Zone ── */
          <motion.div
            key="upload"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.97 }}
            transition={{ duration: 0.35 }}
          >
            <Card>
              <CardContent className="p-8">
                <motion.div
                  onDragOver={handleDragOver}
                  onDragLeave={handleDragLeave}
                  onDrop={handleDrop}
                  animate={isDragging ? { scale: 1.02 } : { scale: 1 }}
                  transition={{ duration: 0.2 }}
                  className={`flex flex-col items-center justify-center rounded-2xl border-2 border-dashed p-14 text-center transition-all duration-300 ${
                    isDragging
                      ? 'border-brand-500 bg-brand-500/5 shadow-glow-sm'
                      : 'border-ink-200 dark:border-white/10 hover:border-brand-400/50 dark:hover:border-brand-500/30 hover:bg-ink-50 dark:hover:bg-white/[0.02]'
                  }`}
                >
                  <motion.div
                    animate={isDragging ? { scale: 1.15, y: -4 } : { scale: 1, y: 0 }}
                    transition={{ duration: 0.25 }}
                    className="mb-5 flex h-16 w-16 items-center justify-center rounded-2xl bg-brand-500/10 text-brand-500"
                  >
                    <CloudUpload size={32} />
                  </motion.div>
                  <h3 className="mb-2 text-lg font-semibold text-ink-900 dark:text-white">
                    {isDragging ? 'Drop it here!' : 'Drag & drop your resume'}
                  </h3>
                  <p className="mb-6 text-sm text-ink-400 dark:text-ink-500">
                    Supports PDF, DOCX — Max 5MB
                  </p>
                  <Button onClick={simulateUpload} isLoading={isUploading} variant="gradient">
                    <Upload size={15} /> Browse Files
                  </Button>
                </motion.div>
              </CardContent>
            </Card>
          </motion.div>
        ) : (
          /* ── Uploaded View ── */
          <motion.div
            key="uploaded"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            transition={{ duration: 0.4 }}
            className="grid grid-cols-1 gap-6 lg:grid-cols-3"
          >
            <div className="lg:col-span-2 space-y-5">
              {/* File card */}
              <Card hoverable>
                <CardHeader className="flex flex-row items-center justify-between">
                  <div className="flex items-center gap-4">
                    <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-brand-500/10 text-brand-500">
                      <FileText size={22} />
                    </div>
                    <div>
                      <CardTitle>{mockResume.fileName}</CardTitle>
                      <CardDescription>Uploaded successfully • Parsed by AI</CardDescription>
                    </div>
                  </div>
                  <Button variant="ghost" size="icon" onClick={() => setUploaded(false)}>
                    <X size={18} />
                  </Button>
                </CardHeader>
                <CardContent>
                  <div className="rounded-xl border border-ink-100 dark:border-white/[0.06] bg-ink-50 dark:bg-white/[0.02] p-5">
                    <div className="flex items-center justify-between mb-4">
                      <h4 className="font-semibold text-ink-900 dark:text-white text-sm">ATS Score</h4>
                      <Badge variant="success" className="text-sm px-3 py-1">
                        {mockResume.score} / 100
                      </Badge>
                    </div>
                    <ProgressBar value={mockResume.score} color="emerald" showValue={false} size="lg" delay={0.3} />
                    <p className="mt-3 text-sm text-ink-500 dark:text-ink-400 leading-relaxed">
                      Your resume is well-structured and highlights key technical skills effectively.
                    </p>
                  </div>
                </CardContent>
              </Card>

              {/* Experience */}
              <Card hoverable>
                <CardHeader>
                  <CardTitle>Extracted Experience</CardTitle>
                  <CardDescription>AI-parsed work history from your resume</CardDescription>
                </CardHeader>
                <CardContent className="space-y-5">
                  {mockResume.detectedExperience.map((exp, i) => (
                    <motion.div
                      key={i}
                      initial={{ opacity: 0, x: -10 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ delay: 0.4 + i * 0.08 }}
                      className="flex gap-4"
                    >
                      <div className="mt-2 flex h-2 w-2 shrink-0 rounded-full bg-brand-500" />
                      <div>
                        <h4 className="font-semibold text-ink-900 dark:text-white text-sm">{exp.role}</h4>
                        <p className="text-sm text-ink-400 mt-0.5">{exp.company} • {exp.duration}</p>
                      </div>
                    </motion.div>
                  ))}
                </CardContent>
              </Card>
            </div>

            <div className="space-y-5">
              {/* Skills */}
              <Card hoverable>
                <CardHeader>
                  <CardTitle>Detected Skills</CardTitle>
                  <CardDescription>Technologies identified from your resume</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="flex flex-wrap gap-2">
                    {mockResume.extractedSkills.map((skill, i) => (
                      <motion.div
                        key={i}
                        initial={{ opacity: 0, scale: 0.8 }}
                        animate={{ opacity: 1, scale: 1 }}
                        transition={{ delay: 0.5 + i * 0.04 }}
                      >
                        <Badge variant="secondary">{skill}</Badge>
                      </motion.div>
                    ))}
                  </div>
                </CardContent>
              </Card>

              {/* Projects */}
              <Card hoverable>
                <CardHeader>
                  <CardTitle>Detected Projects</CardTitle>
                  <CardDescription>Projects identified from your resume</CardDescription>
                </CardHeader>
                <CardContent className="space-y-3">
                  {mockResume.detectedProjects.map((proj, i) => (
                    <motion.div
                      key={i}
                      initial={{ opacity: 0, y: 8 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ delay: 0.6 + i * 0.07 }}
                      className="rounded-xl border border-ink-100 dark:border-white/[0.06] p-3 hover:border-brand-500/30 transition-colors"
                    >
                      <h4 className="text-sm font-semibold text-ink-900 dark:text-white">{proj.name}</h4>
                      <p className="mt-1 text-xs text-ink-400 dark:text-ink-500">{proj.tech}</p>
                    </motion.div>
                  ))}
                </CardContent>
              </Card>

              <Button
                className="w-full gap-2"
                variant="gradient"
                onClick={() => navigate('/interview/company')}
              >
                Continue to Interview Setup <CheckCircle2 size={16} />
              </Button>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}